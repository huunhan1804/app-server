// ReportServiceImpl.java
package com.example.shoppingsystem.services;

import com.example.shoppingsystem.dtos.reports.*;
import com.example.shoppingsystem.entities.*;
import com.example.shoppingsystem.repositories.*;
import com.example.shoppingsystem.services.interfaces.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final AgencyInfoRepository agencyInfoRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<PlatformRevenueReportDTO> getMonthlyRevenueReport(LocalDate startDate, LocalDate endDate) {
        List<PlatformRevenueReportDTO> reports = new ArrayList<>();

        YearMonth start = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);

        while (!start.isAfter(end)) {
            LocalDate monthStart = start.atDay(1);
            LocalDate monthEnd = start.atEndOfMonth();

            // Tính doanh thu giao dịch
            BigDecimal transactionRevenue = orderRepository
                    .findOrdersInDateRange(monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59))
                    .stream()
                    .filter(order -> "DELIVERED".equals(order.getOrderStatus().toString()))
                    .map(OrderList::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Tính hoa hồng từ agency (giả sử 5% trên mỗi giao dịch)
            BigDecimal agencyCommission = transactionRevenue.multiply(new BigDecimal("0.05"));

            // Tính doanh thu subscription (giả sử từ AgencyInfo)
            BigDecimal subscriptionRevenue = calculateSubscriptionRevenue(monthStart, monthEnd);

            // Premium listing revenue (tạm thời để 0 vì chưa có trong DB)
            BigDecimal premiumRevenue = BigDecimal.ZERO;

            // Operating costs (giả sử 20% tổng doanh thu)
            BigDecimal totalRevenue = transactionRevenue.add(agencyCommission).add(subscriptionRevenue);
            BigDecimal operatingCosts = totalRevenue.multiply(new BigDecimal("0.20"));

            reports.add(new PlatformRevenueReportDTO(
                    monthStart,
                    transactionRevenue,
                    agencyCommission,
                    subscriptionRevenue,
                    premiumRevenue,
                    operatingCosts,
                    totalRevenue.subtract(operatingCosts)
            ));

            start = start.plusMonths(1);
        }

        return reports;
    }

    @Override
    public List<PlatformRevenueReportDTO> getQuarterlyRevenueReport(int year) {
        List<PlatformRevenueReportDTO> quarterlyReports = new ArrayList<>();

        for (int quarter = 1; quarter <= 4; quarter++) {
            LocalDate quarterStart = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
            LocalDate quarterEnd = quarterStart.plusMonths(3).minusDays(1);

            List<PlatformRevenueReportDTO> monthlyData = getMonthlyRevenueReport(quarterStart, quarterEnd);

            // Aggregate quarterly data
            BigDecimal totalTransaction = monthlyData.stream()
                    .map(PlatformRevenueReportDTO::getTotalTransactionRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCommission = monthlyData.stream()
                    .map(PlatformRevenueReportDTO::getAgencyCommissionRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalSubscription = monthlyData.stream()
                    .map(PlatformRevenueReportDTO::getSubscriptionRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalOperating = monthlyData.stream()
                    .map(PlatformRevenueReportDTO::getOperatingCosts)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            quarterlyReports.add(new PlatformRevenueReportDTO(
                    quarterStart,
                    totalTransaction,
                    totalCommission,
                    totalSubscription,
                    BigDecimal.ZERO,
                    totalOperating,
                    totalTransaction.add(totalCommission).add(totalSubscription).subtract(totalOperating)
            ));
        }

        return quarterlyReports;
    }

    @Override
    public List<PlatformRevenueReportDTO> getYearlyRevenueReport(int startYear, int endYear) {
        List<PlatformRevenueReportDTO> yearlyReports = new ArrayList<>();

        for (int year = startYear; year <= endYear; year++) {
            LocalDate yearStart = LocalDate.of(year, 1, 1);
            LocalDate yearEnd = LocalDate.of(year, 12, 31);

            List<PlatformRevenueReportDTO> quarterlyData = getQuarterlyRevenueReport(year);

            // Aggregate yearly data
            BigDecimal totalTransaction = quarterlyData.stream()
                    .map(PlatformRevenueReportDTO::getTotalTransactionRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCommission = quarterlyData.stream()
                    .map(PlatformRevenueReportDTO::getAgencyCommissionRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalSubscription = quarterlyData.stream()
                    .map(PlatformRevenueReportDTO::getSubscriptionRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalOperating = quarterlyData.stream()
                    .map(PlatformRevenueReportDTO::getOperatingCosts)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            yearlyReports.add(new PlatformRevenueReportDTO(
                    yearStart,
                    totalTransaction,
                    totalCommission,
                    totalSubscription,
                    BigDecimal.ZERO,
                    totalOperating,
                    totalTransaction.add(totalCommission).add(totalSubscription).subtract(totalOperating)
            ));
        }

        return yearlyReports;
    }

    @Override
    public UserAnalyticsDTO getUserEngagementReport(LocalDate startDate, LocalDate endDate) {
        // Tính toán từ dữ liệu đơn hàng và người dùng hiện có
        List<OrderList> orders = orderRepository.findOrdersInDateRange(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );

        Set<Long> uniqueUsers = orders.stream()
                .map(order -> order.getAccount().getAccountId())
                .collect(Collectors.toSet());

        Long totalSessions = (long) uniqueUsers.size();
        Long totalPageViews = totalSessions * 8; // Giả sử mỗi session có 8 page views

        // Conversion rate: số người mua / tổng số visitor
        Long totalVisitors = accountRepository.countByCreatedDateBetween(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );

        BigDecimal conversionRate = totalVisitors > 0 ?
                new BigDecimal(uniqueUsers.size()).divide(new BigDecimal(totalVisitors), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)) : BigDecimal.ZERO;

        return new UserAnalyticsDTO(
                "User Engagement Summary",
                totalSessions,
                totalPageViews,
                new BigDecimal("5.5"), // Giả sử 5.5 phút/session
                new BigDecimal("35.2"), // Giả sử bounce rate 35.2%
                conversionRate,
                "Mixed",
                "Vietnam"
        );
    }

    @Override
    public Map<String, Object> getConversionRateAnalysis(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analysis = new HashMap<>();

        List<OrderList> orders = orderRepository.findOrdersInDateRange(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );

        // Visitors to customers conversion
        Long totalCustomers = (long) orders.stream()
                .map(order -> order.getAccount().getAccountId())
                .collect(Collectors.toSet()).size();

        Long totalVisitors = accountRepository.count();

        BigDecimal visitorToCustomer = totalVisitors > 0 ?
                new BigDecimal(totalCustomers).divide(new BigDecimal(totalVisitors), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)) : BigDecimal.ZERO;

        // Browser to buyer conversion
        Long totalBuyers = (long) orders.stream()
                .filter(order -> "DELIVERED".equals(order.getOrderStatus().toString()))
                .map(order -> order.getAccount().getAccountId())
                .collect(Collectors.toSet()).size();

        BigDecimal browserToBuyer = totalCustomers > 0 ?
                new BigDecimal(totalBuyers).divide(new BigDecimal(totalCustomers), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)) : BigDecimal.ZERO;

        analysis.put("visitorToCustomerRate", visitorToCustomer);
        analysis.put("browserToBuyerRate", browserToBuyer);
        analysis.put("totalVisitors", totalVisitors);
        analysis.put("totalCustomers", totalCustomers);
        analysis.put("totalBuyers", totalBuyers);

        return analysis;
    }

    @Override
    public List<ProductAnalyticsDTO> getTopSellingProducts(int limit, LocalDate startDate, LocalDate endDate) {
        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(product -> {
                    List<OrderList> productOrders = orderRepository.findOrdersInDateRange(
                                    startDate.atStartOfDay(),
                                    endDate.atTime(23, 59, 59)
                            ).stream()
                            .filter(order -> order.getOrderDetails().stream()
                                    .anyMatch(detail -> detail.getProduct().getProductId().equals(product.getProductId())))
                            .collect(Collectors.toList());

                    Integer orderCount = productOrders.size();
                    BigDecimal revenue = productOrders.stream()
                            .flatMap(order -> order.getOrderDetails().stream())
                            .filter(detail -> detail.getProduct().getProductId().equals(product.getProductId()))
                            .map(detail -> detail.getPrice().multiply(new BigDecimal(detail.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new ProductAnalyticsDTO(
                            product.getProductName(),
                            product.getCategory().getCategoryName(),
                            0,
                            orderCount,
                            revenue,
                            BigDecimal.ZERO, // Conversion rate calculation would need view tracking
                            0
                    );
                })
                .sorted((a, b) -> b.getOrderCount().compareTo(a.getOrderCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public byte[] exportRevenueReportToExcel(ReportRequestDTO request) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Revenue Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Date", "Transaction Revenue", "Commission Revenue",
                    "Subscription Revenue", "Operating Costs", "Net Profit"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Get data based on report type
            List<PlatformRevenueReportDTO> reportData;
            switch (request.getReportType()) {
                case "monthly":
                    reportData = getMonthlyRevenueReport(request.getStartDate(), request.getEndDate());
                    break;
                case "quarterly":
                    reportData = getQuarterlyRevenueReport(request.getStartDate().getYear());
                    break;
                case "yearly":
                    reportData = getYearlyRevenueReport(request.getStartDate().getYear(), request.getEndDate().getYear());
                    break;
                default:
                    reportData = getMonthlyRevenueReport(request.getStartDate(), request.getEndDate());
            }

            // Fill data rows
            int rowNum = 1;
            for (PlatformRevenueReportDTO report : reportData) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(report.getReportDate().toString());
                row.createCell(1).setCellValue(report.getTotalTransactionRevenue().doubleValue());
                row.createCell(2).setCellValue(report.getAgencyCommissionRevenue().doubleValue());
                row.createCell(3).setCellValue(report.getSubscriptionRevenue().doubleValue());
                row.createCell(4).setCellValue(report.getOperatingCosts().doubleValue());
                row.createCell(5).setCellValue(report.getNetProfit().doubleValue());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error creating Excel report", e);
        }
    }

    private BigDecimal calculateSubscriptionRevenue(LocalDate startDate, LocalDate endDate) {
        // Giả sử agency trả phí subscription hàng tháng
        List<AgencyInfo> agencies = agencyInfoRepository.findAll();
        return new BigDecimal(agencies.size()).multiply(new BigDecimal("500000")); // 500k VND/tháng
    }

    // Implement other methods...
    @Override
    public Map<String, Long> getGeographicDistribution() {
        // Implementation tạm thời - trong thực tế cần bảng địa lý riêng
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("Ho Chi Minh City", 45L);
        distribution.put("Hanoi", 30L);
        distribution.put("Da Nang", 15L);
        distribution.put("Other", 10L);
        return distribution;
    }

    @Override
    public Map<String, Long> getDeviceAnalytics() {
        // Implementation tạm thời - trong thực tế cần tracking device
        Map<String, Long> devices = new HashMap<>();
        devices.put("Mobile", 60L);
        devices.put("Desktop", 30L);
        devices.put("Tablet", 10L);
        return devices;
    }

    @Override
    public List<ProductAnalyticsDTO> getTrendingCategories(LocalDate startDate, LocalDate endDate) {
        // Implementation similar to top selling products but grouped by category
        return categoryRepository.findAll().stream()
                .map(category -> {
                    List<Product> categoryProducts = productRepository.findByCategory_CategoryId(category.getCategoryId());
                    Integer totalViews = categoryProducts.stream()
                            .mapToInt(p -> 0)
                            .sum();

                    return new ProductAnalyticsDTO(
                            "Category: " + category.getCategoryName(),
                            category.getCategoryName(),
                            totalViews,
                            0, // Order count calculation needed
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            0
                    );
                })
                .sorted((a, b) -> b.getViewCount().compareTo(a.getViewCount()))
                .collect(Collectors.toList());
    }

    @Override
    public byte[] exportUserAnalyticsToExcel(ReportRequestDTO request) {
        // Implementation similar to exportRevenueReportToExcel
        // but for user analytics data
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("User Analytics Report");

            // Create headers and data...
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error creating User Analytics Excel report", e);
        }
    }
}