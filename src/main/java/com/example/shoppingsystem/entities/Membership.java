// Membership.java - Entity mới cho hệ thống membership
package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "membership")
public class Membership extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBERSHIP_ID")
    private Long membershipId;

    @OneToOne
    @JoinColumn(name = "ACCOUNT_ID", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "MEMBERSHIP_LEVEL")
    private MembershipLevel membershipLevel = MembershipLevel.BRONZE;

    @Column(name = "TOTAL_SPENT", precision = 15, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "TOTAL_ORDERS")
    private Integer totalOrders = 0;

    @Column(name = "POINTS_EARNED")
    private Integer pointsEarned = 0;

    @Column(name = "POINTS_USED")
    private Integer pointsUsed = 0;

    @Column(name = "POINTS_BALANCE")
    private Integer pointsBalance = 0;

    @Column(name = "LEVEL_UP_DATE")
    private LocalDateTime levelUpDate;

    @Column(name = "NEXT_LEVEL_REQUIREMENT", precision = 15, scale = 2)
    private BigDecimal nextLevelRequirement;

    @Column(name = "DISCOUNT_PERCENTAGE", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "FREE_SHIPPING")
    private Boolean freeShipping = false;

    @Column(name = "PRIORITY_SUPPORT")
    private Boolean prioritySupport = false;

    @Column(name = "EARLY_ACCESS")
    private Boolean earlyAccess = false;

    public enum MembershipLevel {
        BRONZE, SILVER, GOLD, DIAMOND
    }
}