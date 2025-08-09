package com.example.shoppingsystem.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgencyDashboardDTO {
    private double revenueToday;
    private double revenueWeek;
    private double revenueMonth;
    private int totalOrders;
    private int newOrders;
    private int processingOrders;
    private int activeProducts;
    private int outOfStockProducts;
    private int pendingProducts;
}