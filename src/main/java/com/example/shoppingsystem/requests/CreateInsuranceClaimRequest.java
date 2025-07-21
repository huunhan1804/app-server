package com.example.shoppingsystem.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInsuranceClaimRequest {

    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    private Long orderId;

    @NotBlank(message = "Tiêu đề yêu cầu không được để trống")
    private String claimTitle;

    @NotBlank(message = "Mô tả chi tiết không được để trống")
    private String claimDescription;

    @NotBlank(message = "Mức độ nghiêm trọng không được để trống")
    private String severityLevel; // LOW, MEDIUM, HIGH, CRITICAL

    private String customerIdCardFrontUrl;
    private String customerIdCardBackUrl;
    private List<String> medicalBillUrls;
    private List<String> testResultUrls;
    private List<String> doctorReportUrls;
    private List<String> otherEvidenceUrls;
}