package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceClaimDTO {
    private Long claimId;
    private String claimCode;

    // Customer info
    private Long customerId; // Thêm để dùng trong communication
    private String customerName;
    private String customerEmail;

    // Agency info
    private Long agencyId; // Thêm để dùng trong communication
    private String agencyName;
    private String agencyEmail;

    // Product info
    private String productName;

    // Claim details
    private String claimTitle;
    private String claimDescription;
    private String severityLevel;
    private String claimStatus;

    // Evidence
    private String customerIdCardFrontUrl;
    private String customerIdCardBackUrl;
    private List<String> medicalBillUrls;
    private List<String> testResultUrls;
    private List<String> doctorReportUrls;
    private List<String> otherEvidenceUrls;

    // Processing info
    private BigDecimal compensationAmount;
    private String compensationType;
    private String rejectionReason;
    private String adminNotes;
    private String processedByName;
    private LocalDateTime processedDate;
    private LocalDateTime submittedDate;

    // Communications
    private List<InsuranceClaimCommunicationDTO> communications;
}