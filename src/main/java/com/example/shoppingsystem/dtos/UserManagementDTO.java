// UserManagementDTO.java
package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserManagementDTO {
    private Long accountId;
    private String username;
    private String fullname;
    private String email;
    private String phone;
    private String role;
    private String accountStatus;
    private boolean isBanned;
    private LocalDateTime createdDate;
    private LocalDateTime lastLogin;

    // Customer specific
    private BigDecimal totalOrderValue;
    private Integer totalOrders;
    private String membershipLevel;

    // Agency specific
    private String businessName;
    private String approvalStatus;
    private BigDecimal totalRevenue;
    private Double storeRating;
    private Integer totalReviews;
    private LocalDateTime submittedDate;
    private String rejectionReason;
}