package com.example.shoppingsystem.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GenerateReportRequest {
    private String reportType; // "quarterly", "monthly", "yearly"
    private int year;
    private int period; // quarter (1-4) or month (1-12)
}