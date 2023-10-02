package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.Otp;
import com.example.shoppingsystem.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp,Long> {
    Optional<Otp> findByLoginIdAndOtpPurpose(String loginId, OtpPurpose otpPurpose);
    @Modifying
    @Query(value = "DELETE FROM otp WHERE expiry_date <= :currentTime", nativeQuery = true)
    void deleteAllByExpiryDate(LocalDateTime currentTime);
}
