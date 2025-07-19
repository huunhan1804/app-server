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
public class ProductManagementDTO {
    private Long productId;
    private String productName;
    private String productDescription;
    private String imageUrl;
    private String agencyName;
    private String agencyEmail;
    private String categoryName;
    private BigDecimal listPrice;
    private BigDecimal salePrice;
    private Integer inventoryQuantity;
    private Integer soldAmount;
    private String status;
    private String statusName;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}