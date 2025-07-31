package com.example.shoppingsystem.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {
    private String reportType; // "revenue", "products", "user-analytics"
    private String period; // "daily", "monthly", "quarterly", "yearly"
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer year;
    private Integer limit; // For top products limit
}