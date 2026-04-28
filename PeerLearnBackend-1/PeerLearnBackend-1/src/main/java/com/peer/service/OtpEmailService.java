package com.peer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OtpEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public OtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendLoginOtpEmail(String toEmail, String fullName, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(fromEmail);
        message.setSubject("PeerLearn Login OTP");
        message.setText("Hello " + fullName + ",\n\n"
                + "Your login OTP is: " + otpCode + "\n\n"
                + "This OTP expires in 10 minutes.\n"
                + "If you did not request this, ignore this email.");

        mailSender.send(message);
    }
}
