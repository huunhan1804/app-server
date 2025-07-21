package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "seller_registration_application")
public class AgencyInfo extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "APPLICATION_ID")
    private Long applicationId;

    @OneToOne
    @JoinColumn(name = "ACCOUNT_ID", nullable = false, unique = true)
    private Account account;

    @Column(name = "SHOP_NAME")
    private String shopName;

    @Column(name = "SHOP_ADDRESS_DETAIL")
    private String shopAddressDetail;

    @Column(name = "SHOP_EMAIL")
    private String shopEmail;

    @Column(name = "SHOP_PHONE")
    private String shopPhone;

    @Column(name = "TAX_NUMBER")
    private String taxNumber;

    @Column(name = "FULLNAME_APPLICANT")
    private String fullNameApplicant;

    @Column(name = "BIRTHDATE_APPLICANT")
    private LocalDate birthdateApplicant;

    @Column(name = "GENDER_APPLICANT")
    private String genderApplicant;

    @Column(name = "IDCARD_NUMBER")
    private String idCardNumber;

    @Column(name = "DATE_OF_ISSUE_IDCARD")
    private LocalDate dateOfIssueIdCard;

    @Column(name = "PLACE_OF_ISSUE_IDCARD")
    private String placeOfIssueIdCard;

    @Column(name = "IDCARD_FRONT_IMAGE_URL")
    private String idCardFrontImageUrl;

    @Column(name = "IDCARD_BACK_IMAGE_URL")
    private String idCardBackImageUrl;

    @Column(name = "BUSINESS_LICENSE_URLS")
    private String businessLicenseUrls;

    @Column(name = "PROFESSIONAL_CERT_URLS")
    private String professionalCertUrls;

    @Column(name= "DIPLOMA_CERT_URLS")
    private String diplomaCertUrls;

    @ManyToOne
    @JoinColumn(name = "STATUS_ID", nullable = false)
    private ApprovalStatus approvalStatus;

    @Column(name = "REJECTION_REASON")
    private String rejectionReason;

    @Column(name = "SUBMITTED_DATE")
    private LocalDate submittedDate;


}
