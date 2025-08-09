package com.example.shoppingsystem.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevenueComparisonDTO {
    private BigDecimal currentPeriodRevenue;
    private BigDecimal previousPeriodRevenue;
    private double growthRate;
    private int currentPeriodOrders;
    private int previousPeriodOrders;
}
