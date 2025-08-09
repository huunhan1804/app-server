package com.example.shoppingsystem.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderAnalyticsDTO {
    private String period;
    private int year;
    private int totalOrders;
    private List<OrderStatusAnalytics> statusAnalytics;
}