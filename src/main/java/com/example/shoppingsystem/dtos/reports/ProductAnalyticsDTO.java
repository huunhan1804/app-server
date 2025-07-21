package com.example.shoppingsystem.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductAnalyticsDTO {
    private String productName;
    private String categoryName;
    private Integer viewCount;
    private Integer orderCount;
    private BigDecimal revenue;
    private BigDecimal conversionRate;
    private Integer rank;
}
