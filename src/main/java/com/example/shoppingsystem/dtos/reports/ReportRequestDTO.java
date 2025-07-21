package com.example.shoppingsystem.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequestDTO {
    private String reportType; // monthly, quarterly, yearly
    private LocalDate startDate;
    private LocalDate endDate;
    private String category;
    private String region;
}
