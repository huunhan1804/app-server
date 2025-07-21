// AgencyApplicationDetailDTO.java
package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgencyApplicationDetailDTO {
    private Long applicationId;
    private Long accountId;
    private String username;
    private String fullname;
    private String email;
    private String phone;

    // Shop information
    private String shopName;
    private String shopAddressDetail;
    private String shopEmail;
    private String shopPhone;
    private String taxNumber;

    // Applicant information
    private String fullNameApplicant;
    private Date birthdateApplicant;
    private String genderApplicant;
    private String idCardNumber;
    private String dateOfIssueIdCard;
    private String placeOfIssueIdCard;

    // Documents
    private String idCardFrontImageUrl;
    private String idCardBackImageUrl;
    private String businessLicenseUrls;
    private String professionalCertUrls;
    private String diplomaCertUrls;

    // Status
    private String statusCode;
    private String statusName;
    private String rejectionReason;
    private LocalDateTime submittedDate;
    private LocalDateTime reviewedDate;
    private String reviewedBy;
}