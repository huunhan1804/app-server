package com.example.shoppingsystem.entities;

import com.example.shoppingsystem.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "otp")
public class Otp extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OTP_ID")
    private Long otpId;

    @Column(name = "LOGIN_ID", nullable = false)
    private String loginId;

    @Column(name = "OTP_CODE", nullable = false)
    private String otpCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "OTP_PURPOSE", nullable = false)
    private OtpPurpose otpPurpose;

    @Column(name = "EXPIRY_DATE", nullable = false)
    private LocalDateTime expiryDate;
}
