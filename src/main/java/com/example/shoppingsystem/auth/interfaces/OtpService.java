package com.example.shoppingsystem.auth.interfaces;

import com.example.shoppingsystem.entities.Otp;
import com.example.shoppingsystem.enums.OtpPurpose;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface OtpService {
    Otp createOtp(String loginId, OtpPurpose otpPurpose);
    boolean isOtpValid(String otpCode, String loginId, OtpPurpose otpPurpose);
    @Async
    void deleteOtp(Otp otp);
    Optional<Otp> getOtpByLoginIdAndPurpose(String loginId, OtpPurpose otpPurpose);
    boolean canRequestOtp(String loginId, OtpPurpose otpPurpose);
}
