package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "support_feedback")
public class SupportFeedback extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FEEDBACK_ID")
    private Long feedbackId;

    @ManyToOne
    @JoinColumn(name = "ARTICLE_ID", nullable = false)
    private SupportArticle supportArticle;

    @ManyToOne
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    @Column(name = "IS_HELPFUL")
    private Boolean isHelpful;
}
