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
@Table(name = "chat_message")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAT_MESSAGE_ID")
    private long chatMessageId;

    @ManyToOne
    @JoinColumn(name = "CHAT_SESSION_ID", referencedColumnName = "CHAT_SESSION_ID", nullable = false)
    private ChatSession chatSession;

    @ManyToOne
    @JoinColumn(name = "SENDER_ID", referencedColumnName = "ACCOUNT_ID", nullable = false)
    private Account sender;

    @ManyToOne
    @JoinColumn(name = "RECEIVER_ID", referencedColumnName = "ACCOUNT_ID", nullable = false)
    private Account receiver;

    @Column(name = "MESSAGE_CONTENT")
    private String messageContent;

    @Column(name = "SENT_AT")
    private LocalDateTime sendAt;

    @Column(name = "IS_READ")
    private boolean isRead;
}
