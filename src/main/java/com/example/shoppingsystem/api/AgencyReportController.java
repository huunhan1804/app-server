package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.reports.*;
import com.example.shoppingsystem.requests.GenerateReportRequest;
import com.example.shoppingsystem.requests.RevenueComparisonRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.AgencyReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agency/reports")
@RequiredArgsConstructor
@Tag(name = "Agency Reports")
@PreAuthorize("hasRole('agency')")
public class AgencyReportController {
    private final AgencyReportService agencyReportService;

    @Operation(summary = "Get Agency Dashboard Data", description = "Get dashboard overview data for agency")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AgencyDashboardDTO>> getDashboardData() {
        ApiResponse<AgencyDashboardDTO> response = agencyReportService.getDashboardData();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Generate Revenue Report", description = "Generate revenue report by type and period")
    @PostMapping("/revenue")
    public ResponseEntity<ApiResponse<ReportResponseDTO>> generateRevenueReport(
            @RequestBody GenerateReportRequest request
    ) {
        ApiResponse<ReportResponseDTO> response = agencyReportService.generateRevenueReport(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Get Revenue Trend", description = "Get revenue trend data for charts")
    @GetMapping("/revenue-trend")
    public ResponseEntity<ApiResponse<RevenueTrendDTO>> getRevenueTrend(
            @Parameter(description = "Period type: daily, monthly, yearly")
            @RequestParam String period,
            @Parameter(description = "Year")
            @RequestParam int year
    ) {
        ApiResponse<RevenueTrendDTO> response = agencyReportService.getRevenueTrend(period, year);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Get Product Performance", description = "Get product performance analytics")
    @GetMapping("/product-performance")
    public ResponseEntity<ApiResponse<ProductPerformanceDTO>> getProductPerformance(
            @Parameter(description = "Year")
            @RequestParam int year,
            @Parameter(description = "Month (1-12)")
            @RequestParam int month
    ) {
        ApiResponse<ProductPerformanceDTO> response = agencyReportService.getProductPerformance(year, month);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Get Order Analytics", description = "Get order analytics data")
    @GetMapping("/order-analytics")
    public ResponseEntity<ApiResponse<OrderAnalyticsDTO>> getOrderAnalytics(
            @Parameter(description = "Period type: monthly, quarterly, yearly")
            @RequestParam String period,
            @Parameter(description = "Year")
            @RequestParam int year
    ) {
        ApiResponse<OrderAnalyticsDTO> response = agencyReportService.getOrderAnalytics(period, year);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Export Report to PDF", description = "Export revenue report to PDF file")
    @PostMapping("/export/pdf")
    public ResponseEntity<ByteArrayResource> exportReportToPdf(
            @RequestBody GenerateReportRequest request
    ) {
        try {
            byte[] pdfData = agencyReportService.exportReportToPdf(request);

            ByteArrayResource resource = new ByteArrayResource(pdfData);

            String filename = String.format("BaoCao_%s_%s.pdf",
                    request.getReportType(), request.getYear());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfData.length)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Export Report to Excel", description = "Export revenue report to Excel file")
    @PostMapping("/export/excel")
    public ResponseEntity<ByteArrayResource> exportReportToExcel(
            @RequestBody GenerateReportRequest request
    ) {
        try {
            byte[] excelData = agencyReportService.exportReportToExcel(request);

            ByteArrayResource resource = new ByteArrayResource(excelData);

            String filename = String.format("BaoCao_%s_%s.xlsx",
                    request.getReportType(), request.getYear());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelData.length)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Additional endpoints for comprehensive reporting

    @Operation(summary = "Get Quick Stats", description = "Get quick statistics for dashboard widgets")
    @GetMapping("/quick-stats")
    public ResponseEntity<ApiResponse<QuickStatsDTO>> getQuickStats() {
        // This can reuse dashboard data or create a separate method
        ApiResponse<AgencyDashboardDTO> dashboardResponse = agencyReportService.getDashboardData();

        if (dashboardResponse.getStatus() == 200) {
            AgencyDashboardDTO dashboard = dashboardResponse.getData();
            QuickStatsDTO quickStats = QuickStatsDTO.builder()
                    .totalRevenue(dashboard.getRevenueMonth())
                    .totalOrders(dashboard.getTotalOrders())
                    .activeProducts(dashboard.getActiveProducts())
                    .processingOrders(dashboard.getProcessingOrders())
                    .build();

            return ResponseEntity.ok(ApiResponse.<QuickStatsDTO>builder()
                    .status(200)
                    .message("Success")
                    .data(quickStats)
                    .timestamp(new java.util.Date())
                    .build());
        }

        return ResponseEntity.status(dashboardResponse.getStatus())
                .body(ApiResponse.<QuickStatsDTO>builder()
                        .status(dashboardResponse.getStatus())
                        .message(dashboardResponse.getMessage())
                        .timestamp(new java.util.Date())
                        .build());
    }

    @Operation(summary = "Get Revenue Comparison", description = "Compare revenue between periods")
    @PostMapping("/revenue-comparison")
    public ResponseEntity<ApiResponse<RevenueComparisonDTO>> getRevenueComparison(
            @RequestBody RevenueComparisonRequest request
    ) {
        try {
            // Generate reports for both periods
            GenerateReportRequest currentPeriodRequest = GenerateReportRequest.builder()
                    .reportType(request.getReportType())
                    .year(request.getCurrentYear())
                    .period(request.getCurrentPeriod())
                    .build();

            GenerateReportRequest previousPeriodRequest = GenerateReportRequest.builder()
                    .reportType(request.getReportType())
                    .year(request.getPreviousYear())
                    .period(request.getPreviousPeriod())
                    .build();

            ApiResponse<ReportResponseDTO> currentResponse = agencyReportService.generateRevenueReport(currentPeriodRequest);
            ApiResponse<ReportResponseDTO> previousResponse = agencyReportService.generateRevenueReport(previousPeriodRequest);

            if (currentResponse.getStatus() == 200 && previousResponse.getStatus() == 200) {
                ReportSummaryDTO currentSummary = currentResponse.getData().getSummary();
                ReportSummaryDTO previousSummary = previousResponse.getData().getSummary();

                double growthRate = 0.0;
                if (previousSummary.getTotalRevenue().doubleValue() > 0) {
                    growthRate = ((currentSummary.getTotalRevenue().doubleValue() -
                            previousSummary.getTotalRevenue().doubleValue()) /
                            previousSummary.getTotalRevenue().doubleValue()) * 100;
                }

                RevenueComparisonDTO comparison = RevenueComparisonDTO.builder()
                        .currentPeriodRevenue(currentSummary.getTotalRevenue())
                        .previousPeriodRevenue(previousSummary.getTotalRevenue())
                        .growthRate(growthRate)
                        .currentPeriodOrders(currentSummary.getTotalOrders())
                        .previousPeriodOrders(previousSummary.getTotalOrders())
                        .build();

                return ResponseEntity.ok(ApiResponse.<RevenueComparisonDTO>builder()
                        .status(200)
                        .message("Success")
                        .data(comparison)
                        .timestamp(new java.util.Date())
                        .build());
            }

            return ResponseEntity.badRequest().body(ApiResponse.<RevenueComparisonDTO>builder()
                    .status(400)
                    .message("Could not generate comparison data")
                    .timestamp(new java.util.Date())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<RevenueComparisonDTO>builder()
                            .status(500)
                            .message("Error generating comparison: " + e.getMessage())
                            .timestamp(new java.util.Date())
                            .build());
        }
    }

    @Operation(summary = "Get Top Products", description = "Get top performing products")
    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<List<TopProductDTO>>> getTopProducts(
            @Parameter(description = "Year")
            @RequestParam int year,
            @Parameter(description = "Month (1-12)")
            @RequestParam int month,
            @Parameter(description = "Limit number of results")
            @RequestParam(defaultValue = "10") int limit
    ) {
        ApiResponse<ProductPerformanceDTO> response = agencyReportService.getProductPerformance(year, month);

        if (response.getStatus() == 200) {
            List<TopProductDTO> topProducts = response.getData().getProducts().stream()
                    .limit(limit)
                    .map(item -> TopProductDTO.builder()
                            .productName(item.getProductName())
                            .revenue(item.getRevenue())
                            .quantitySold(item.getQuantitySold())
                            .build())
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(ApiResponse.<List<TopProductDTO>>builder()
                    .status(200)
                    .message("Success")
                    .data(topProducts)
                    .timestamp(new java.util.Date())
                    .build());
        }

        return ResponseEntity.status(response.getStatus())
                .body(ApiResponse.<List<TopProductDTO>>builder()
                        .status(response.getStatus())
                        .message(response.getMessage())
                        .timestamp(new java.util.Date())
                        .build());
    }

    @Operation(summary = "Get Charts Data", description = "Get data for all dashboard charts")
    @GetMapping("/charts-data")
    public ResponseEntity<ApiResponse<DashboardChartsDTO>> getChartsData(
            @Parameter(description = "Year")
            @RequestParam int year
    ) {
        try {
            // Get revenue trend for the year
            ApiResponse<RevenueTrendDTO> trendResponse = agencyReportService.getRevenueTrend("monthly", year);

            // Get order analytics
            ApiResponse<OrderAnalyticsDTO> analyticsResponse = agencyReportService.getOrderAnalytics("yearly", year);

            if (trendResponse.getStatus() == 200 && analyticsResponse.getStatus() == 200) {
                DashboardChartsDTO chartsData = DashboardChartsDTO.builder()
                        .revenueTrend(trendResponse.getData())
                        .orderAnalytics(analyticsResponse.getData())
                        .build();

                return ResponseEntity.ok(ApiResponse.<DashboardChartsDTO>builder()
                        .status(200)
                        .message("Success")
                        .data(chartsData)
                        .timestamp(new java.util.Date())
                        .build());
            }

            return ResponseEntity.badRequest().body(ApiResponse.<DashboardChartsDTO>builder()
                    .status(400)
                    .message("Could not load charts data")
                    .timestamp(new java.util.Date())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<DashboardChartsDTO>builder()
                            .status(500)
                            .message("Error loading charts data: " + e.getMessage())
                            .timestamp(new java.util.Date())
                            .build());
        }
    }
}