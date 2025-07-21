package com.example.shoppingsystem.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessInsuranceClaimRequest {

    @NotBlank(message = "Trạng thái xử lý không được để trống")
    private String claimStatus; // APPROVED, REJECTED, PENDING_DOCUMENTS, etc.

    private BigDecimal compensationAmount;
    private String compensationType; // CASH, VOUCHER, PRODUCT_REPLACEMENT
    private String rejectionReason;
    private String adminNotes;
}