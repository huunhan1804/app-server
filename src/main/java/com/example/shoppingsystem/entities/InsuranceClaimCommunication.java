package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "insurance_claim_communication")
public class InsuranceClaimCommunication extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMUNICATION_ID")
    private Long communicationId;

    @ManyToOne
    @JoinColumn(name = "CLAIM_ID", nullable = false)
    private InsuranceClaim insuranceClaim;

    @ManyToOne
    @JoinColumn(name = "SENDER_ID", nullable = false)
    private Account sender;

    @ManyToOne
    @JoinColumn(name = "RECIPIENT_ID", nullable = false)
    private Account recipient;

    @Column(name = "EMAIL_SUBJECT", nullable = false)
    private String emailSubject;

    @Column(name = "EMAIL_CONTENT", columnDefinition = "TEXT")
    private String emailContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "COMMUNICATION_TYPE")
    private CommunicationType communicationType;

    @Column(name = "SENT_DATE")
    private LocalDateTime sentDate = LocalDateTime.now();

    @Column(name = "IS_READ")
    private Boolean isRead = false;

    public enum CommunicationType {
        ADMIN_TO_CUSTOMER, ADMIN_TO_AGENCY,
        CUSTOMER_TO_ADMIN, AGENCY_TO_ADMIN
    }
}