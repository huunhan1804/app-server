package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.reports.*;
import com.example.shoppingsystem.requests.GenerateReportRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import org.springframework.stereotype.Service;

@Service
public interface AgencyReportService {
    /**
     * Get dashboard overview data for current agency
     */
    ApiResponse<AgencyDashboardDTO> getDashboardData();

    /**
     * Generate revenue report based on type and period
     */
    ApiResponse<ReportResponseDTO> generateRevenueReport(GenerateReportRequest request);

    /**
     * Export report to PDF format
     */
    byte[] exportReportToPdf(GenerateReportRequest request) throws Exception;

    /**
     * Export report to Excel format
     */
    byte[] exportReportToExcel(GenerateReportRequest request) throws Exception;

    /**
     * Get revenue trend data for charts
     */
    ApiResponse<RevenueTrendDTO> getRevenueTrend(String period, int year);

    /**
     * Get product performance data
     */
    ApiResponse<ProductPerformanceDTO> getProductPerformance(int year, int month);

    /**
     * Get order analytics data
     */
    ApiResponse<OrderAnalyticsDTO> getOrderAnalytics(String period, int year);
}