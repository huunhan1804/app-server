package com.example.shoppingsystem.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevenueComparisonRequest {
    private String reportType;
    private int currentYear;
    private int currentPeriod;
    private int previousYear;
    private int previousPeriod;
}