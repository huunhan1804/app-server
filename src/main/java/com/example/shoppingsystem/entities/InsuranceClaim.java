package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "insurance_claim")
public class InsuranceClaim extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLAIM_ID")
    private Long claimId;

    @Column(name = "CLAIM_CODE", unique = true, nullable = false)
    private String claimCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Account customer; // Account với role = 'customer'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AGENCY_ID", nullable = false)
    private Account agency; // Account với role = 'agency' (owner của product)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID")
    private OrderList order;

    @Column(name = "CLAIM_TITLE", nullable = false)
    private String claimTitle;

    @Column(name = "CLAIM_DESCRIPTION", columnDefinition = "TEXT")
    private String claimDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "SEVERITY_LEVEL")
    private SeverityLevel severityLevel = SeverityLevel.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "CLAIM_STATUS")
    private ClaimStatus claimStatus = ClaimStatus.SUBMITTED;

    @Column(name = "CUSTOMER_IDCARD_FRONT_URL")
    private String customerIdCardFrontUrl;

    @Column(name = "CUSTOMER_IDCARD_BACK_URL")
    private String customerIdCardBackUrl;

    @Column(name = "MEDICAL_BILL_URLS", columnDefinition = "TEXT")
    private String medicalBillUrls;

    @Column(name = "TEST_RESULT_URLS", columnDefinition = "TEXT")
    private String testResultUrls;

    @Column(name = "DOCTOR_REPORT_URLS", columnDefinition = "TEXT")
    private String doctorReportUrls;

    @Column(name = "OTHER_EVIDENCE_URLS", columnDefinition = "TEXT")
    private String otherEvidenceUrls;

    @Column(name = "COMPENSATION_AMOUNT", precision = 15, scale = 2)
    private BigDecimal compensationAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "COMPENSATION_TYPE")
    private CompensationType compensationType;

    @Column(name = "REJECTION_REASON", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "ADMIN_NOTES", columnDefinition = "TEXT")
    private String adminNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESSED_BY")
    private Account processedBy; // Account với role = 'admin'

    @Column(name = "PROCESSED_DATE")
    private LocalDateTime processedDate;

    @Column(name = "SUBMITTED_DATE")
    private LocalDateTime submittedDate = LocalDateTime.now();

    @OneToMany(mappedBy = "insuranceClaim", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InsuranceClaimCommunication> communications;

    public enum SeverityLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum ClaimStatus {
        SUBMITTED, UNDER_REVIEW, PENDING_DOCUMENTS,
        APPROVED, REJECTED, CLOSED, CANCELLED
    }

    public enum CompensationType {
        CASH, VOUCHER, PRODUCT_REPLACEMENT
    }
}