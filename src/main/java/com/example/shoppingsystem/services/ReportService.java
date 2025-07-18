package com.example.shoppingsystem.services;


import com.example.shoppingsystem.entities.ReportData;
import com.example.shoppingsystem.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    public List<ReportData> generateAnnualProductReport(int year) {
        List<Object[]> results = reportRepository.generateAnnualProductReport(year);
        List<ReportData> reports = new ArrayList<>();

        for (Object[] result : results) {
            ReportData report = new ReportData();
            report.setPeriod(String.valueOf(result[0]));
            report.setTotalProducts(((Number) result[1]).longValue());
            report.setTotalRevenue((BigDecimal) result[2]);
            report.setTotalOrders(((Number) result[3]).longValue());
            report.setProductName((String) result[4]);
            report.setCategoryName((String) result[5]);
            reports.add(report);
        }
        return reports;
    }

    public List<ReportData> generateQuarterlyAgencyReport(int year, int quarter) {
        List<Object[]> results = reportRepository.generateQuarterlyAgencyReport(year, quarter);
        List<ReportData> reports = new ArrayList<>();

        for (Object[] result : results) {
            ReportData report = new ReportData();
            report.setPeriod((String) result[0]);
            report.setAgencyName((String) result[1]);
            report.setTotalOrders(((Number) result[2]).longValue());
            report.setTotalRevenue((BigDecimal) result[3]);
            report.setAverageOrderValue((BigDecimal) result[4]);
            report.setTotalCustomers(((Number) result[5]).longValue());
            reports.add(report);
        }
        return reports;
    }

    public List<ReportData> generateMonthlySalesReport(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = reportRepository.generateMonthlySalesReport(startDate, endDate);
        List<ReportData> reports = new ArrayList<>();

        for (Object[] result : results) {
            ReportData report = new ReportData();
            report.setPeriod((String) result[0]);
            report.setTotalOrders(((Number) result[1]).longValue());
            report.setTotalRevenue((BigDecimal) result[2]);
            report.setAverageOrderValue((BigDecimal) result[3]);
            reports.add(report);
        }
        return reports;
    }

    public Map<String, Object> createRevenueAnalyticsReport(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = reportRepository.createRevenueAnalyticsReport(startDate, endDate);
        List<ReportData> dailyReports = new ArrayList<>();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        Long totalOrders = 0L;
        BigDecimal totalShippingFee = BigDecimal.ZERO;
        BigDecimal totalInsuranceFee = BigDecimal.ZERO;
        BigDecimal totalDiscounts = BigDecimal.ZERO;

        for (Object[] result : results) {
            ReportData report = new ReportData();
            report.setPeriod(result[0].toString());
            report.setTotalOrders(((Number) result[1]).longValue());
            report.setTotalRevenue((BigDecimal) result[2]);
            report.setAverageOrderValue((BigDecimal) result[3]);

            totalRevenue = totalRevenue.add((BigDecimal) result[2]);
            totalOrders += ((Number) result[1]).longValue();
            totalShippingFee = totalShippingFee.add((BigDecimal) result[4]);
            totalInsuranceFee = totalInsuranceFee.add((BigDecimal) result[5]);
            totalDiscounts = totalDiscounts.add((BigDecimal) result[6]);

            dailyReports.add(report);
        }

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("dailyReports", dailyReports);
        analytics.put("summary", Map.of(
                "totalRevenue", totalRevenue,
                "totalOrders", totalOrders,
                "averageOrderValue", totalOrders > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO,
                "totalShippingFee", totalShippingFee,
                "totalInsuranceFee", totalInsuranceFee,
                "totalDiscounts", totalDiscounts
        ));

        return analytics;
    }

    public List<ReportData> trackSalesPerformance(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = reportRepository.trackSalesPerformance(startDate, endDate);
        List<ReportData> reports = new ArrayList<>();

        for (Object[] result : results) {
            ReportData report = new ReportData();
            report.setAgencyName((String) result[0]);
            report.setTotalOrders(result[1] != null ? ((Number) result[1]).longValue() : 0L);
            report.setTotalRevenue(result[2] != null ? (BigDecimal) result[2] : BigDecimal.ZERO);
            report.setAverageOrderValue(result[3] != null ? (BigDecimal) result[3] : BigDecimal.ZERO);
            // Thêm các trường khác nếu cần
            reports.add(report);
        }
        return reports;
    }
}