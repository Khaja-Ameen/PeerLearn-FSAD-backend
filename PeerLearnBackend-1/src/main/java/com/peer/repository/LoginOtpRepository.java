package com.peer.repository;

import com.peer.entity.LoginOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginOtpRepository extends JpaRepository<LoginOtp, Long> {
    Optional<LoginOtp> findTopByUser_IdAndUsedFalseOrderByCreatedAtDesc(Long userId);
    Optional<LoginOtp> findByUser_IdAndOtpCodeAndUsedFalse(Long userId, String otpCode);
    void deleteByUser_Id(Long userId);
}
