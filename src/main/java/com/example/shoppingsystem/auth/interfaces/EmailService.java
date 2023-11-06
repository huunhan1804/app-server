package com.example.shoppingsystem.auth.interfaces;

import com.example.shoppingsystem.enums.OtpPurpose;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public interface EmailService {
    @Async
    void sendPasswordEmail(String email, String password);
    @Async
    void sendOTP(String email, String otpCode, OtpPurpose otpPurpose);

}
