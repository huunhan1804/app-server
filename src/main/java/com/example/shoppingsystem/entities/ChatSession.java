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
@Table(name = "chat_session")
public class ChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAT_SESSION_ID")
    private long chatSessionId;

    @ManyToOne
    @JoinColumn(name = "CUSTOMER_ID", referencedColumnName = "ACCOUNT_ID", nullable = false)
    private Account customer;

    @ManyToOne
    @JoinColumn(name = "AGENCY_ID", referencedColumnName = "ACCOUNT_ID", nullable = false)
    private Account agency;

    @Column(name = "STARTED_AT")
    private LocalDateTime startedAt;

    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;
}
