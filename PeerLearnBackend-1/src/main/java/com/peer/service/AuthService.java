package com.peer.service;

import com.peer.dto.AuthResponse;
import com.peer.dto.LoginRequest;
import com.peer.dto.RegisterRequest;
import com.peer.entity.User;
import com.peer.repository.UserRepository;
import com.peer.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (request.getRole() == null) {
            throw new RuntimeException("Role is required");
        }

        String finalUserId;

        if (request.getRole() == User.Role.STUDENT) {
            String incomingStudentId = request.getStudentId() != null && !request.getStudentId().isBlank()
                    ? request.getStudentId().trim()
                    : (request.getUserId() != null ? request.getUserId().trim() : "");

            if (!incomingStudentId.matches("\\d{10}")) {
                throw new RuntimeException("Student ID must be exactly 10 digits");
            }

            // Check for duplicate Student ID
            if (userRepository.findByUserId(incomingStudentId).isPresent()) {
                throw new RuntimeException("This Student ID is already registered. Please use a different ID.");
            }

            finalUserId = incomingStudentId;

        } else if (request.getRole() == User.Role.TEACHER) {
            String incomingFacultyId = request.getFacultyId() != null && !request.getFacultyId().isBlank()
                    ? request.getFacultyId().trim()
                    : (request.getUserId() != null ? request.getUserId().trim() : "");

            if (!incomingFacultyId.matches("\\d{4}")) {
                throw new RuntimeException("Faculty ID must be exactly 4 digits");
            }

            // Check for duplicate Faculty ID
            if (userRepository.findByUserId(incomingFacultyId).isPresent()) {
                throw new RuntimeException("This Faculty ID is already registered. Please use a different ID.");
            }

            finalUserId = incomingFacultyId;

        } else {
            throw new RuntimeException("Invalid role");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .section(request.getSection())
                .department(request.getDepartment())
                .userId(finalUserId)
                .active(true)
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user);
    }
}