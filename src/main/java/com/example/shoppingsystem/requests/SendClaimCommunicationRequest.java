package com.example.shoppingsystem.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendClaimCommunicationRequest {

    @NotNull(message = "Claim ID không được để trống")
    private Long claimId;

    @NotNull(message = "Recipient ID không được để trống")
    private Long recipientId;

    @NotBlank(message = "Tiêu đề email không được để trống")
    private String emailSubject;

    @NotBlank(message = "Nội dung email không được để trống")
    private String emailContent;

    @NotBlank(message = "Loại communication không được để trống")
    private String communicationType; // ADMIN_TO_CUSTOMER, ADMIN_TO_AGENCY
}