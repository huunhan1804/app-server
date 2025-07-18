// ReportController.java - cập nhật
package com.example.shoppingsystem.api;

import com.example.shoppingsystem.entities.ReportData;
import com.example.shoppingsystem.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReportController {

    @Autowired
    private ReportService reportService;

    // Test endpoint để kiểm tra kết nối
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Reports API is working",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/annual-product/{year}")
    public ResponseEntity<?> generateAnnualProductReport(@PathVariable int year) {
        try {
            List<ReportData> reports = reportService.generateAnnualProductReport(year);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", reports,
                    "count", reports.size(),
                    "year", year
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }

    @GetMapping("/quarterly-agency/{year}/{quarter}")
    public ResponseEntity<?> generateQuarterlyAgencyReport(
            @PathVariable int year,
            @PathVariable int quarter) {
        try {
            if (quarter < 1 || quarter > 4) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Quarter must be between 1 and 4",
                        "provided", quarter
                ));
            }
            List<ReportData> reports = reportService.generateQuarterlyAgencyReport(year, quarter);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", reports,
                    "count", reports.size(),
                    "period", year + "-Q" + quarter
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }

    @GetMapping("/monthly-sales")
    public ResponseEntity<?> generateMonthlySalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<ReportData> reports = reportService.generateMonthlySalesReport(startDate, endDate);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", reports,
                    "count", reports.size(),
                    "period", startDate + " to " + endDate
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }

    @GetMapping("/revenue-analytics")
    public ResponseEntity<?> createRevenueAnalyticsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Map<String, Object> analytics = reportService.createRevenueAnalyticsReport(startDate, endDate);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", analytics,
                    "period", startDate + " to " + endDate
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }

    @GetMapping("/sales-performance")
    public ResponseEntity<?> trackSalesPerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<ReportData> reports = reportService.trackSalesPerformance(startDate, endDate);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", reports,
                    "count", reports.size(),
                    "period", startDate + " to " + endDate
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardReports() {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(12);
            int currentYear = endDate.getYear();
            int currentQuarter = (endDate.getMonthValue() - 1) / 3 + 1;

            Map<String, Object> dashboard = Map.of(
                    "monthlySales", reportService.generateMonthlySalesReport(startDate, endDate),
                    "annualProducts", reportService.generateAnnualProductReport(currentYear),
                    "quarterlyAgency", reportService.generateQuarterlyAgencyReport(currentYear, currentQuarter),
                    "revenueAnalytics", reportService.createRevenueAnalyticsReport(startDate, endDate),
                    "salesPerformance", reportService.trackSalesPerformance(startDate, endDate)
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", dashboard,
                    "generated_at", java.time.LocalDateTime.now(),
                    "period", "Last 12 months"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }
}