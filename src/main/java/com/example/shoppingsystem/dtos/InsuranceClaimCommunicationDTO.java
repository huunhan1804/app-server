package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceClaimCommunicationDTO {
    private Long communicationId;
    private Long claimId;
    private String senderName;
    private String senderEmail;
    private String recipientName;
    private String recipientEmail;
    private String emailSubject;
    private String emailContent;
    private String communicationType;
    private LocalDateTime sentDate;
    private Boolean isRead;
}