package com.example.shoppingsystem.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuickStatsDTO {
    private double totalRevenue;
    private int totalOrders;
    private int activeProducts;
    private int processingOrders;
}
