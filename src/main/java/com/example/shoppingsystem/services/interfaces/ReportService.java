// ReportService.java
package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.reports.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public interface ReportService {
    // Platform Revenue Reports
    List<PlatformRevenueReportDTO> getMonthlyRevenueReport(LocalDate startDate, LocalDate endDate);
    List<PlatformRevenueReportDTO> getQuarterlyRevenueReport(int year);
    List<PlatformRevenueReportDTO> getYearlyRevenueReport(int startYear, int endYear);

    // User Analytics
    UserAnalyticsDTO getUserEngagementReport(LocalDate startDate, LocalDate endDate);
    Map<String, Object> getConversionRateAnalysis(LocalDate startDate, LocalDate endDate);
    Map<String, Long> getGeographicDistribution();
    Map<String, Long> getDeviceAnalytics();

    // Product Analytics
    List<ProductAnalyticsDTO> getTopSellingProducts(int limit, LocalDate startDate, LocalDate endDate);
    List<ProductAnalyticsDTO> getTrendingCategories(LocalDate startDate, LocalDate endDate);

    // Export functionality
    byte[] exportRevenueReportToExcel(ReportRequestDTO request);
    byte[] exportUserAnalyticsToExcel(ReportRequestDTO request);
}

