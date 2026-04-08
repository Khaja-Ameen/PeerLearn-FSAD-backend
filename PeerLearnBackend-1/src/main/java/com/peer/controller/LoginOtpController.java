package com.peer.controller;

import com.peer.dto.AuthResponse;
import com.peer.dto.LoginOtpRequest;
import com.peer.dto.OtpResponse;
import com.peer.dto.VerifyOtpRequest;
import com.peer.service.LoginOtpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginOtpController {

    private final LoginOtpService loginOtpService;

    public LoginOtpController(LoginOtpService loginOtpService) {
        this.loginOtpService = loginOtpService;
    }

    @PostMapping("/login-otp")
    public ResponseEntity<?> loginOtp(@Valid @RequestBody LoginOtpRequest request) {
        try {
            OtpResponse response = loginOtpService.requestLoginOtp(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of(
                            "timestamp", Instant.now().toString(),
                            "status", 401,
                            "error", "Unauthorized",
                            "message", ex.getMessage()
                    )
            );
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            AuthResponse response = loginOtpService.verifyLoginOtp(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of(
                            "timestamp", Instant.now().toString(),
                            "status", 401,
                            "error", "Unauthorized",
                            "message", ex.getMessage()
                    )
            );
        }
    }
}
