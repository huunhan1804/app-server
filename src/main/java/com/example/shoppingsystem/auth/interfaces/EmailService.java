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
    @Async
    void sendNotificationEmail(String email, String fullName, String title, String message);

    @Async
    void sendProductNotificationEmail(String email, String fullName, String productName, String title, String message);
    @Async
    void sendInsuranceClaimNotification(String email, String fullName, String subject, String message, String claimCode);
    @Async
    void sendInsuranceClaimCommunication(String email, String fullName, String subject, String content, String claimCode);


}
