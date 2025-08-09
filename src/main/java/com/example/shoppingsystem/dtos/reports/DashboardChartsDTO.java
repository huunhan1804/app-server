package com.example.shoppingsystem.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardChartsDTO {
    private RevenueTrendDTO revenueTrend;
    private OrderAnalyticsDTO orderAnalytics;
}