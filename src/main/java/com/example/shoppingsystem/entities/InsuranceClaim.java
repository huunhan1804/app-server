package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "insurance_claim")
public class InsuranceClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLAIM_ID")
    private Long claimId;

    @Column(name = "ACCOUNT_ID", nullable = false)
    private Long accountId;

    @Column(name = "ORDER_ID", nullable = false)
    private Long orderId;

    @Column(name = "PRODUCT_ID", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "CLAIM_TYPE", nullable = false)
    private ClaimType claimType;

    @Column(name = "CLAIM_DESCRIPTION", columnDefinition = "TEXT", nullable = false)
    private String claimDescription;

    @Column(name = "CLAIM_AMOUNT", precision = 15, scale = 2)
    private BigDecimal claimAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "CLAIM_STATUS")
    private ClaimStatus claimStatus = ClaimStatus.SUBMITTED;

    @Column(name = "MEDICAL_EVIDENCE", columnDefinition = "TEXT")
    private String medicalEvidence;

    @Column(name = "IDENTITY_PROOF")
    private String identityProof;

    @Column(name = "ADDITIONAL_DOCUMENTS", columnDefinition = "TEXT")
    private String additionalDocuments;

    @Column(name = "ADMIN_NOTES", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "APPROVAL_DATE")
    private LocalDateTime approvalDate;

    @Column(name = "APPROVED_BY")
    private Long approvedBy;

    @Column(name = "REJECTION_REASON", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "PAYOUT_AMOUNT", precision = 15, scale = 2)
    private BigDecimal payoutAmount;

    @Column(name = "PAYOUT_DATE")
    private LocalDateTime payoutDate;

    @Column(name = "PAYOUT_METHOD")
    private String payoutMethod;

    @Column(name = "PAYOUT_REFERENCE")
    private String payoutReference;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate = LocalDateTime.now();

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID", insertable = false, updatable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID", insertable = false, updatable = false)
    private OrderList orderList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", insertable = false, updatable = false)
    private Product product;

    // Enums
    public enum ClaimType {
        SIDE_EFFECT, PRODUCT_DEFECT, HEALTH_ISSUE, OTHER
    }

    public enum ClaimStatus {
        SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, PAID
    }

    // Constructors
    public InsuranceClaim() {}

    // Getters and Setters
    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public ClaimType getClaimType() { return claimType; }
    public void setClaimType(ClaimType claimType) { this.claimType = claimType; }

    public String getClaimDescription() { return claimDescription; }
    public void setClaimDescription(String claimDescription) { this.claimDescription = claimDescription; }

    public BigDecimal getClaimAmount() { return claimAmount; }
    public void setClaimAmount(BigDecimal claimAmount) { this.claimAmount = claimAmount; }

    public ClaimStatus getClaimStatus() { return claimStatus; }
    public void setClaimStatus(ClaimStatus claimStatus) { this.claimStatus = claimStatus; }

    public String getMedicalEvidence() { return medicalEvidence; }
    public void setMedicalEvidence(String medicalEvidence) { this.medicalEvidence = medicalEvidence; }

    public String getIdentityProof() { return identityProof; }
    public void setIdentityProof(String identityProof) { this.identityProof = identityProof; }

    public String getAdditionalDocuments() { return additionalDocuments; }
    public void setAdditionalDocuments(String additionalDocuments) { this.additionalDocuments = additionalDocuments; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public LocalDateTime getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDateTime approvalDate) { this.approvalDate = approvalDate; }

    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public BigDecimal getPayoutAmount() { return payoutAmount; }
    public void setPayoutAmount(BigDecimal payoutAmount) { this.payoutAmount = payoutAmount; }

    public LocalDateTime getPayoutDate() { return payoutDate; }
    public void setPayoutDate(LocalDateTime payoutDate) { this.payoutDate = payoutDate; }

    public String getPayoutMethod() { return payoutMethod; }
    public void setPayoutMethod(String payoutMethod) { this.payoutMethod = payoutMethod; }

    public String getPayoutReference() { return payoutReference; }
    public void setPayoutReference(String payoutReference) { this.payoutReference = payoutReference; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public OrderList getOrderList() { return orderList; }
    public void setOrderList(OrderList orderList) { this.orderList = orderList; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}