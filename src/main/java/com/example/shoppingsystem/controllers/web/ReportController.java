// ReportController.java
package com.example.shoppingsystem.controllers.web;

import com.example.shoppingsystem.dtos.reports.*;
import com.example.shoppingsystem.services.interfaces.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("")
    public String reportsPage() {
        return "admin/reports";
    }

    @GetMapping("/")
    public String reportsPageWithSlash() {
        return "redirect:/admin/reports"; // Redirect về reports chính
    }

    @GetMapping("/api/revenue")
    @ResponseBody
    public Map<String, Object> getRevenueReport(@RequestParam String type,
                                                @RequestParam(required = false) String startDate,
                                                @RequestParam(required = false) String endDate,
                                                @RequestParam(required = false) Integer year) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<PlatformRevenueReportDTO> reportData;

            switch (type) {
                case "monthly":
                    LocalDate start = LocalDate.parse(startDate);
                    LocalDate end = LocalDate.parse(endDate);
                    reportData = reportService.getMonthlyRevenueReport(start, end);
                    break;
                case "quarterly":
                    reportData = reportService.getQuarterlyRevenueReport(year != null ? year : LocalDate.now().getYear());
                    break;
                case "yearly":
                    int startYear = year != null ? year : LocalDate.now().getYear() - 5;
                    int endYear = LocalDate.now().getYear();
                    reportData = reportService.getYearlyRevenueReport(startYear, endYear);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid report type: " + type);
            }

            response.put("success", true);
            response.put("data", reportData);
            response.put("message", "Report generated successfully");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating report: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/api/user-analytics")
    @ResponseBody
    public Map<String, Object> getUserAnalytics(@RequestParam String startDate,
                                                @RequestParam String endDate) {
        Map<String, Object> response = new HashMap<>();

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            UserAnalyticsDTO analytics = reportService.getUserEngagementReport(start, end);
            Map<String, Object> conversionData = reportService.getConversionRateAnalysis(start, end);
            Map<String, Long> geoData = reportService.getGeographicDistribution();
            Map<String, Long> deviceData = reportService.getDeviceAnalytics();

            response.put("success", true);
            response.put("engagement", analytics);
            response.put("conversion", conversionData);
            response.put("geographic", geoData);
            response.put("devices", deviceData);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting user analytics: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/api/products")
    @ResponseBody
    public Map<String, Object> getProductAnalytics(@RequestParam(defaultValue = "10") int limit,
                                                   @RequestParam String startDate,
                                                   @RequestParam String endDate) {
        Map<String, Object> response = new HashMap<>();

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            List<ProductAnalyticsDTO> topProducts = reportService.getTopSellingProducts(limit, start, end);
            List<ProductAnalyticsDTO> trendingCategories = reportService.getTrendingCategories(start, end);

            response.put("success", true);
            response.put("topProducts", topProducts);
            response.put("trendingCategories", trendingCategories);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting product analytics: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/api/export/revenue")
    public ResponseEntity<byte[]> exportRevenueReport(@RequestBody ReportRequestDTO request) {
        try {
            byte[] excelData = reportService.exportRevenueReportToExcel(request);

            String filename = "revenue_report_" + request.getReportType() + "_" +
                    LocalDate.now().toString() + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelData);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/api/export/user-analytics")
    public ResponseEntity<byte[]> exportUserAnalytics(@RequestBody ReportRequestDTO request) {
        try {
            byte[] excelData = reportService.exportUserAnalyticsToExcel(request);

            String filename = "user_analytics_" + LocalDate.now().toString() + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelData);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}