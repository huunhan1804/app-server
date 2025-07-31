package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.reports.*;
import com.example.shoppingsystem.services.interfaces.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class AdminReportsApiController {

    private final ReportService reportService;

    // ==================== REVENUE REPORTS ====================

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueReport(
            @RequestParam String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer year) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<PlatformRevenueReportDTO> reportData;

            switch (period.toLowerCase()) {
                case "daily":
                    if (startDate != null && endDate != null) {
                        LocalDate start = LocalDate.parse(startDate);
                        LocalDate end = LocalDate.parse(endDate);
                        reportData = reportService.getMonthlyRevenueReport(start, end);
                    } else {
                        // Default to last 30 days
                        LocalDate end = LocalDate.now();
                        LocalDate start = end.minusDays(30);
                        reportData = reportService.getMonthlyRevenueReport(start, end);
                    }
                    break;

                case "monthly":
                    if (startDate != null && endDate != null) {
                        LocalDate start = LocalDate.parse(startDate);
                        LocalDate end = LocalDate.parse(endDate);
                        reportData = reportService.getMonthlyRevenueReport(start, end);
                    } else {
                        // Default to current year
                        int currentYear = LocalDate.now().getYear();
                        LocalDate start = LocalDate.of(currentYear, 1, 1);
                        LocalDate end = LocalDate.of(currentYear, 12, 31);
                        reportData = reportService.getMonthlyRevenueReport(start, end);
                    }
                    break;

                case "quarterly":
                    int yearForQuarterly = year != null ? year : LocalDate.now().getYear();
                    reportData = reportService.getQuarterlyRevenueReport(yearForQuarterly);
                    break;

                case "yearly":
                    int startYear = year != null ? year : LocalDate.now().getYear() - 5;
                    int endYear = LocalDate.now().getYear();
                    reportData = reportService.getYearlyRevenueReport(startYear, endYear);
                    break;

                default:
                    throw new IllegalArgumentException("Invalid period: " + period);
            }

            // Calculate summary statistics
            Map<String, Object> summary = calculateRevenueSummary(reportData);

            response.put("success", true);
            response.put("data", reportData);
            response.put("summary", summary);
            response.put("period", period);
            response.put("message", "Revenue report generated successfully");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating revenue report: " + e.getMessage());
            response.put("data", List.of());
            response.put("summary", Map.of());
        }

        return ResponseEntity.ok(response);
    }

    // ==================== PRODUCT ANALYTICS ====================

    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getProductAnalytics(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Map<String, Object> response = new HashMap<>();

        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

            List<ProductAnalyticsDTO> topProducts = reportService.getTopSellingProducts(limit, start, end);
            List<ProductAnalyticsDTO> trendingCategories = reportService.getTrendingCategories(start, end);

            // Calculate product summary
            Map<String, Object> summary = calculateProductSummary(topProducts, trendingCategories);

            response.put("success", true);
            response.put("topProducts", topProducts);
            response.put("trendingCategories", trendingCategories);
            response.put("summary", summary);
            response.put("period", Map.of("startDate", start, "endDate", end));
            response.put("message", "Product analytics generated successfully");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating product analytics: " + e.getMessage());
            response.put("topProducts", List.of());
            response.put("trendingCategories", List.of());
            response.put("summary", Map.of());
        }

        return ResponseEntity.ok(response);
    }

    // ==================== USER ANALYTICS ====================

    @GetMapping("/user-analytics")
    public ResponseEntity<Map<String, Object>> getUserAnalytics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Map<String, Object> response = new HashMap<>();

        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

            UserAnalyticsDTO engagement = reportService.getUserEngagementReport(start, end);
            Map<String, Object> conversionData = reportService.getConversionRateAnalysis(start, end);
            Map<String, Long> geoData = reportService.getGeographicDistribution();
            Map<String, Long> deviceData = reportService.getDeviceAnalytics();

            // Calculate user behavior summary
            Map<String, Object> summary = calculateUserAnalyticsSummary(engagement, conversionData);

            response.put("success", true);
            response.put("engagement", engagement);
            response.put("conversion", conversionData);
            response.put("geographic", geoData);
            response.put("devices", deviceData);
            response.put("summary", summary);
            response.put("period", Map.of("startDate", start, "endDate", end));
            response.put("message", "User analytics generated successfully");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating user analytics: " + e.getMessage());
            response.put("engagement", null);
            response.put("conversion", Map.of());
            response.put("geographic", Map.of());
            response.put("devices", Map.of());
            response.put("summary", Map.of());
        }

        return ResponseEntity.ok(response);
    }

    // ==================== CHART DATA ENDPOINTS ====================

    @GetMapping("/chart-data/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueChartData(
            @RequestParam String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer year) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Get the same data as revenue report
            ResponseEntity<Map<String, Object>> revenueResponse = getRevenueReport(period, startDate, endDate, year);
            Map<String, Object> revenueData = revenueResponse.getBody();

            if (revenueData != null && (Boolean) revenueData.get("success")) {
                @SuppressWarnings("unchecked")
                List<PlatformRevenueReportDTO> reportData = (List<PlatformRevenueReportDTO>) revenueData.get("data");

                // Format data for Chart.js
                Map<String, Object> chartData = formatRevenueDataForChart(reportData, period);

                response.put("success", true);
                response.put("chartData", chartData);
                response.put("message", "Chart data generated successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to generate chart data");
                response.put("chartData", Map.of());
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating chart data: " + e.getMessage());
            response.put("chartData", Map.of());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/chart-data/products")
    public ResponseEntity<Map<String, Object>> getProductChartData(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Map<String, Object> response = new HashMap<>();

        try {
            ResponseEntity<Map<String, Object>> productResponse = getProductAnalytics(limit, startDate, endDate);
            Map<String, Object> productData = productResponse.getBody();

            if (productData != null && (Boolean) productData.get("success")) {
                @SuppressWarnings("unchecked")
                List<ProductAnalyticsDTO> topProducts = (List<ProductAnalyticsDTO>) productData.get("topProducts");
                @SuppressWarnings("unchecked")
                List<ProductAnalyticsDTO> trendingCategories = (List<ProductAnalyticsDTO>) productData.get("trendingCategories");

                Map<String, Object> chartData = formatProductDataForChart(topProducts, trendingCategories);

                response.put("success", true);
                response.put("chartData", chartData);
                response.put("message", "Product chart data generated successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to generate product chart data");
                response.put("chartData", Map.of());
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating product chart data: " + e.getMessage());
            response.put("chartData", Map.of());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/chart-data/user-behavior")
    public ResponseEntity<Map<String, Object>> getUserBehaviorChartData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Map<String, Object> response = new HashMap<>();

        try {
            ResponseEntity<Map<String, Object>> userResponse = getUserAnalytics(startDate, endDate);
            Map<String, Object> userData = userResponse.getBody();

            if (userData != null && (Boolean) userData.get("success")) {
                @SuppressWarnings("unchecked")
                Map<String, Long> geoData = (Map<String, Long>) userData.get("geographic");
                @SuppressWarnings("unchecked")
                Map<String, Long> deviceData = (Map<String, Long>) userData.get("devices");
                UserAnalyticsDTO engagement = (UserAnalyticsDTO) userData.get("engagement");

                Map<String, Object> chartData = formatUserBehaviorDataForChart(geoData, deviceData, engagement);

                response.put("success", true);
                response.put("chartData", chartData);
                response.put("message", "User behavior chart data generated successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to generate user behavior chart data");
                response.put("chartData", Map.of());
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating user behavior chart data: " + e.getMessage());
            response.put("chartData", Map.of());
        }

        return ResponseEntity.ok(response);
    }

    // ==================== EXPORT ENDPOINTS ====================

    @PostMapping("/export/revenue")
    public ResponseEntity<byte[]> exportRevenueReport(@RequestBody ReportRequestDTO request) {
        try {
            byte[] excelData = reportService.exportRevenueReportToExcel(request);

            String filename = String.format("revenue_report_%s_%s.xlsx",
                    request.getReportType(),
                    LocalDate.now().toString());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelData);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .header("X-Error-Message", "Error exporting revenue report: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/export/products")
    public ResponseEntity<byte[]> exportProductReport(@RequestBody ReportRequestDTO request) {
        try {
            // Since we don't have a specific product export service, we'll use user analytics
            // In a real implementation, you'd create a separate method for product exports
            byte[] excelData = reportService.exportUserAnalyticsToExcel(request);

            String filename = String.format("product_analytics_%s.xlsx", LocalDate.now().toString());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelData);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .header("X-Error-Message", "Error exporting product report: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/export/user-analytics")
    public ResponseEntity<byte[]> exportUserAnalytics(@RequestBody ReportRequestDTO request) {
        try {
            byte[] excelData = reportService.exportUserAnalyticsToExcel(request);

            String filename = String.format("user_analytics_%s.xlsx", LocalDate.now().toString());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelData);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .header("X-Error-Message", "Error exporting user analytics: " + e.getMessage())
                    .build();
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private Map<String, Object> calculateRevenueSummary(List<PlatformRevenueReportDTO> reportData) {
        Map<String, Object> summary = new HashMap<>();

        if (reportData.isEmpty()) {
            summary.put("totalRevenue", "0");
            summary.put("totalTransactions", "0");
            summary.put("totalCommission", "0");
            summary.put("avgRevenue", "0");
            summary.put("growth", "0");
            return summary;
        }

        var totalRevenue = reportData.stream()
                .map(PlatformRevenueReportDTO::getNetProfit)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        var totalTransactions = reportData.stream()
                .map(PlatformRevenueReportDTO::getTotalTransactionRevenue)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        var totalCommission = reportData.stream()
                .map(PlatformRevenueReportDTO::getAgencyCommissionRevenue)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        var avgRevenue = totalRevenue.divide(new java.math.BigDecimal(reportData.size()), 2, java.math.RoundingMode.HALF_UP);

        // Calculate growth rate (comparing first and last period)
        String growth = "0";
        if (reportData.size() > 1) {
            var firstPeriod = reportData.get(0).getNetProfit();
            var lastPeriod = reportData.get(reportData.size() - 1).getNetProfit();
            if (firstPeriod.compareTo(java.math.BigDecimal.ZERO) > 0) {
                var growthRate = lastPeriod.subtract(firstPeriod)
                        .divide(firstPeriod, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(new java.math.BigDecimal(100));
                growth = String.format("%.2f%%", growthRate);
            }
        }

        summary.put("totalRevenue", totalRevenue.toString());
        summary.put("totalTransactions", totalTransactions.toString());
        summary.put("totalCommission", totalCommission.toString());
        summary.put("avgRevenue", avgRevenue.toString());
        summary.put("growth", growth);

        return summary;
    }

    private Map<String, Object> calculateProductSummary(List<ProductAnalyticsDTO> topProducts,
                                                        List<ProductAnalyticsDTO> trendingCategories) {
        Map<String, Object> summary = new HashMap<>();

        int totalProducts = topProducts.size();
        int totalCategories = trendingCategories.size();

        var totalViews = topProducts.stream()
                .mapToInt(ProductAnalyticsDTO::getViewCount)
                .sum();

        var totalOrders = topProducts.stream()
                .mapToInt(ProductAnalyticsDTO::getOrderCount)
                .sum();

        String conversionRate = totalViews > 0 ?
                String.format("%.2f%%", (double) totalOrders / totalViews * 100) : "0%";

        summary.put("totalProducts", totalProducts);
        summary.put("totalCategories", totalCategories);
        summary.put("totalViews", totalViews);
        summary.put("totalOrders", totalOrders);
        summary.put("conversionRate", conversionRate);

        return summary;
    }

    private Map<String, Object> calculateUserAnalyticsSummary(UserAnalyticsDTO engagement,
                                                              Map<String, Object> conversionData) {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalSessions", engagement.getTotalSessions());
        summary.put("totalPageViews", engagement.getTotalPageViews());
        summary.put("avgSessionDuration", engagement.getAverageSessionDuration());
        summary.put("bounceRate", engagement.getBounceRate());
        summary.put("conversionRate", engagement.getConversionRate());

        return summary;
    }

    private Map<String, Object> formatRevenueDataForChart(List<PlatformRevenueReportDTO> reportData, String period) {
        Map<String, Object> chartData = new HashMap<>();

        List<String> labels = reportData.stream()
                .map(data -> data.getReportDate().toString())
                .collect(java.util.stream.Collectors.toList());

        Map<String, Object> datasets = new HashMap<>();

        // Revenue dataset
        Map<String, Object> revenueDataset = new HashMap<>();
        revenueDataset.put("label", "Doanh thu thuần");
        revenueDataset.put("data", reportData.stream()
                .map(PlatformRevenueReportDTO::getNetProfit)
                .collect(java.util.stream.Collectors.toList()));
        revenueDataset.put("borderColor", "#007bff");
        revenueDataset.put("backgroundColor", "rgba(0, 123, 255, 0.1)");

        // Transaction dataset
        Map<String, Object> transactionDataset = new HashMap<>();
        transactionDataset.put("label", "Doanh thu giao dịch");
        transactionDataset.put("data", reportData.stream()
                .map(PlatformRevenueReportDTO::getTotalTransactionRevenue)
                .collect(java.util.stream.Collectors.toList()));
        transactionDataset.put("borderColor", "#28a745");
        transactionDataset.put("backgroundColor", "rgba(40, 167, 69, 0.1)");

        chartData.put("labels", labels);
        chartData.put("datasets", List.of(revenueDataset, transactionDataset));

        return chartData;
    }

    private Map<String, Object> formatProductDataForChart(List<ProductAnalyticsDTO> topProducts,
                                                          List<ProductAnalyticsDTO> trendingCategories) {
        Map<String, Object> chartData = new HashMap<>();

        // Top products chart
        Map<String, Object> topProductsChart = new HashMap<>();
        topProductsChart.put("labels", topProducts.stream()
                .map(ProductAnalyticsDTO::getProductName)
                .collect(java.util.stream.Collectors.toList()));
        topProductsChart.put("data", topProducts.stream()
                .map(ProductAnalyticsDTO::getOrderCount)
                .collect(java.util.stream.Collectors.toList()));

        // Category chart
        Map<String, Object> categoryChart = new HashMap<>();
        categoryChart.put("labels", trendingCategories.stream()
                .map(ProductAnalyticsDTO::getCategoryName)
                .collect(java.util.stream.Collectors.toList()));
        categoryChart.put("data", trendingCategories.stream()
                .map(ProductAnalyticsDTO::getViewCount)
                .collect(java.util.stream.Collectors.toList()));

        chartData.put("topProducts", topProductsChart);
        chartData.put("categories", categoryChart);

        return chartData;
    }

    private Map<String, Object> formatUserBehaviorDataForChart(Map<String, Long> geoData,
                                                               Map<String, Long> deviceData,
                                                               UserAnalyticsDTO engagement) {
        Map<String, Object> chartData = new HashMap<>();

        // Geographic chart
        Map<String, Object> geoChart = new HashMap<>();
        geoChart.put("labels", new java.util.ArrayList<>(geoData.keySet()));
        geoChart.put("data", new java.util.ArrayList<>(geoData.values()));

        // Device chart
        Map<String, Object> deviceChart = new HashMap<>();
        deviceChart.put("labels", new java.util.ArrayList<>(deviceData.keySet()));
        deviceChart.put("data", new java.util.ArrayList<>(deviceData.values()));

        // Engagement metrics
        Map<String, Object> engagementChart = new HashMap<>();
        engagementChart.put("sessions", engagement.getTotalSessions());
        engagementChart.put("pageViews", engagement.getTotalPageViews());
        engagementChart.put("bounceRate", engagement.getBounceRate());
        engagementChart.put("conversionRate", engagement.getConversionRate());

        chartData.put("geographic", geoChart);
        chartData.put("devices", deviceChart);
        chartData.put("engagement", engagementChart);

        return chartData;
    }
}