package com.peer.service;

import com.peer.dto.AuthResponse;
import com.peer.dto.LoginOtpRequest;
import com.peer.dto.VerifyOtpRequest;
import com.peer.dto.OtpResponse;
import com.peer.entity.LoginOtp;
import com.peer.entity.User;
import com.peer.repository.LoginOtpRepository;
import com.peer.repository.UserRepository;
import com.peer.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class LoginOtpService {

    private final UserRepository userRepository;
    private final LoginOtpRepository loginOtpRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final OtpEmailService otpEmailService;

    private static final SecureRandom RANDOM = new SecureRandom();

    public LoginOtpService(UserRepository userRepository,
                           LoginOtpRepository loginOtpRepository,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           OtpEmailService otpEmailService) {
        this.userRepository = userRepository;
        this.loginOtpRepository = loginOtpRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.otpEmailService = otpEmailService;
    }

    @Transactional
    public OtpResponse requestLoginOtp(LoginOtpRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime now = LocalDateTime.now();

        loginOtpRepository.deleteByUser_Id(user.getId());

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        LocalDateTime expiry = now.plusMinutes(10);

        LoginOtp loginOtp = new LoginOtp(user, otp, expiry);
        loginOtpRepository.save(loginOtp);

        otpEmailService.sendLoginOtpEmail(user.getEmail(), user.getFullName(), otp);
        return new OtpResponse("OTP sent to registered email", true);
    }

    @Transactional
    public AuthResponse verifyLoginOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LoginOtp loginOtp = loginOtpRepository.findByUser_IdAndOtpCodeAndUsedFalse(user.getId(), request.getOtp())
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (loginOtp.getExpiryDate().isBefore(LocalDateTime.now())) {
            loginOtpRepository.delete(loginOtp);
            throw new RuntimeException("OTP expired");
        }

        loginOtp.setUsed(true);
        loginOtpRepository.save(loginOtp);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user);
    }
}
