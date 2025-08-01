package com.example.shoppingsystem.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponseDTO {
    private List<AgencyReportDTO> reportData;
    private ReportSummaryDTO summary;
    private String reportType;
    private int year;
    private int period;
    private LocalDateTime generatedDate;
}