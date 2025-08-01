package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.Message;
import com.example.shoppingsystem.dtos.reports.*;
import com.example.shoppingsystem.entities.*;
import com.example.shoppingsystem.repositories.*;
import com.example.shoppingsystem.requests.GenerateReportRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.AccountService;
import com.example.shoppingsystem.services.interfaces.AgencyReportService;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AgencyReportServiceImpl implements AgencyReportService {

    private final AccountService accountService;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AgencyInfoRepository agencyInfoRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    public ApiResponse<AgencyDashboardDTO> getDashboardData() {
        try {
            Optional<Account> currentAgency = accountService.findCurrentUserInfo();
            if (!currentAgency.isPresent()) {
                return buildErrorResponse(ErrorCode.FORBIDDEN, Message.ACCOUNT_NOT_FOUND);
            }

            Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount(currentAgency.get());
            if (!agencyInfo.isPresent()) {
                return buildErrorResponse(ErrorCode.NOT_FOUND, Message.AGENCY_INFO_NOT_FOUND);
            }

            AgencyDashboardDTO dashboard = calculateDashboardMetrics(currentAgency.get());

            return ApiResponse.<AgencyDashboardDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(dashboard)
                    .timestamp(new Date())
                    .build();

        } catch (Exception e) {
            log.error("Error getting dashboard data", e);
            return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Lỗi khi tải dữ liệu dashboard: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<ReportResponseDTO> generateRevenueReport(GenerateReportRequest request) {
        try {
            Optional<Account> currentAgency = accountService.findCurrentUserInfo();
            if (!currentAgency.isPresent()) {
                return buildErrorResponse(ErrorCode.FORBIDDEN, Message.ACCOUNT_NOT_FOUND);
            }

            List<AgencyReportDTO> reportData = generateReportData(currentAgency.get(), request);
            ReportSummaryDTO summary = calculateReportSummary(reportData, request);

            ReportResponseDTO response = ReportResponseDTO.builder()
                    .reportData(reportData)
                    .summary(summary)
                    .reportType(request.getReportType())
                    .year(request.getYear())
                    .period(request.getPeriod())
                    .generatedDate(LocalDateTime.now())
                    .build();

            return ApiResponse.<ReportResponseDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(response)
                    .timestamp(new Date())
                    .build();

        } catch (Exception e) {
            log.error("Error generating revenue report", e);
            return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Lỗi khi tạo báo cáo: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportReportToPdf(GenerateReportRequest request) throws Exception {
        Optional<Account> currentAgency = accountService.findCurrentUserInfo();
        if (!currentAgency.isPresent()) {
            throw new Exception("Agency not found");
        }

        List<AgencyReportDTO> reportData = generateReportData(currentAgency.get(), request);
        ReportSummaryDTO summary = calculateReportSummary(reportData, request);

        return generatePdfReport(reportData, summary, request);
    }

    @Override
    public byte[] exportReportToExcel(GenerateReportRequest request) throws Exception {
        Optional<Account> currentAgency = accountService.findCurrentUserInfo();
        if (!currentAgency.isPresent()) {
            throw new Exception("Agency not found");
        }

        List<AgencyReportDTO> reportData = generateReportData(currentAgency.get(), request);
        ReportSummaryDTO summary = calculateReportSummary(reportData, request);

        return generateExcelReport(reportData, summary, request);
    }

    @Override
    public ApiResponse<RevenueTrendDTO> getRevenueTrend(String period, int year) {
        try {
            Optional<Account> currentAgency = accountService.findCurrentUserInfo();
            if (!currentAgency.isPresent()) {
                return buildErrorResponse(ErrorCode.FORBIDDEN, Message.ACCOUNT_NOT_FOUND);
            }

            List<OrderList> orders = orderRepository.findByAgency_AccountId(currentAgency.get().getAccountId());
            RevenueTrendDTO trendData = calculateRevenueTrend(orders, period, year);

            return ApiResponse.<RevenueTrendDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(trendData)
                    .timestamp(new Date())
                    .build();

        } catch (Exception e) {
            log.error("Error getting revenue trend", e);
            return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Lỗi khi tải xu hướng doanh thu: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<ProductPerformanceDTO> getProductPerformance(int year, int month) {
        try {
            Optional<Account> currentAgency = accountService.findCurrentUserInfo();
            if (!currentAgency.isPresent()) {
                return buildErrorResponse(ErrorCode.FORBIDDEN, Message.ACCOUNT_NOT_FOUND);
            }

            Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount(currentAgency.get());
            if (!agencyInfo.isPresent()) {
                return buildErrorResponse(ErrorCode.NOT_FOUND, Message.AGENCY_INFO_NOT_FOUND);
            }

            ProductPerformanceDTO performance = calculateProductPerformance(agencyInfo.get(), year, month);

            return ApiResponse.<ProductPerformanceDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(performance)
                    .timestamp(new Date())
                    .build();

        } catch (Exception e) {
            log.error("Error getting product performance", e);
            return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Lỗi khi tải hiệu suất sản phẩm: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<OrderAnalyticsDTO> getOrderAnalytics(String period, int year) {
        try {
            Optional<Account> currentAgency = accountService.findCurrentUserInfo();
            if (!currentAgency.isPresent()) {
                return buildErrorResponse(ErrorCode.FORBIDDEN, Message.ACCOUNT_NOT_FOUND);
            }

            List<OrderList> orders = orderRepository.findByAgency_AccountId(currentAgency.get().getAccountId());
            OrderAnalyticsDTO analytics = calculateOrderAnalytics(orders, period, year);

            return ApiResponse.<OrderAnalyticsDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(analytics)
                    .timestamp(new Date())
                    .build();

        } catch (Exception e) {
            log.error("Error getting order analytics", e);
            return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Lỗi khi tải phân tích đơn hàng: " + e.getMessage());
        }
    }

    // Private helper methods

    private AgencyDashboardDTO calculateDashboardMetrics(Account agency) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(7);
        LocalDateTime startOfMonth = now.minusDays(30);

        List<OrderList> allOrders = orderRepository.findByAgency_AccountId(agency.getAccountId());

        // Calculate revenue metrics
        double revenueToday = calculateRevenue(allOrders, startOfToday, now);
        double revenueWeek = calculateRevenue(allOrders, startOfWeek, now);
        double revenueMonth = calculateRevenue(allOrders, startOfMonth, now);

        // Calculate order metrics
        long totalOrders = allOrders.size();
        long newOrders = allOrders.stream()
                .filter(order -> order.getOrderDate().isAfter(startOfToday))
                .count();
        long processingOrders = allOrders.stream()
                .filter(order -> "PENDING".equals(order.getOrderStatus().toString()) ||
                        "CONFIRMED".equals(order.getOrderStatus().toString()))
                .count();

        // Calculate product metrics
        Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount(agency);
        List<Product> products = new ArrayList<>();
        if (agencyInfo.isPresent()) {
            products = productRepository.findByAgencyInfo(agencyInfo.get());
        }

        int activeProducts = (int) products.stream()
                .filter(p -> "approved".equals(p.getApprovalStatus().getStatusCode()))
                .count();
        int outOfStockProducts = (int) products.stream()
                .filter(p -> p.getInventoryQuantity() <= 0)
                .count();
        int pendingProducts = (int) products.stream()
                .filter(p -> "pending".equals(p.getApprovalStatus().getStatusCode()))
                .count();

        return AgencyDashboardDTO.builder()
                .revenueToday(revenueToday)
                .revenueWeek(revenueWeek)
                .revenueMonth(revenueMonth)
                .totalOrders((int) totalOrders)
                .newOrders((int) newOrders)
                .processingOrders((int) processingOrders)
                .activeProducts(activeProducts)
                .outOfStockProducts(outOfStockProducts)
                .pendingProducts(pendingProducts)
                .build();
    }

    private double calculateRevenue(List<OrderList> orders, LocalDateTime startDate, LocalDateTime endDate) {
        return orders.stream()
                .filter(order -> order.getOrderDate().isAfter(startDate) &&
                        order.getOrderDate().isBefore(endDate))
                .filter(order -> "DELIVERED".equals(order.getOrderStatus().toString()))
                .mapToDouble(order -> order.getTotalPrice().doubleValue())
                .sum();
    }

    private List<AgencyReportDTO> generateReportData(Account agency, GenerateReportRequest request) {
        List<OrderList> orders = orderRepository.findByAgency_AccountId(agency.getAccountId());

        switch (request.getReportType().toLowerCase()) {
            case "quarterly":
                return generateQuarterlyReport(orders, request.getYear(), request.getPeriod());
            case "monthly":
                return generateMonthlyReport(orders, request.getYear(), request.getPeriod());
            case "yearly":
                return generateYearlyReport(orders, request.getYear());
            default:
                throw new IllegalArgumentException("Invalid report type: " + request.getReportType());
        }
    }

    private List<AgencyReportDTO> generateQuarterlyReport(List<OrderList> orders, int year, int quarter) {
        List<AgencyReportDTO> reportData = new ArrayList<>();
        int startMonth = (quarter - 1) * 3 + 1;

        for (int i = 0; i < 3; i++) {
            int month = startMonth + i;
            if (month <= 12) {
                LocalDateTime monthStart = LocalDateTime.of(year, month, 1, 0, 0);
                LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

                List<OrderList> monthOrders = orders.stream()
                        .filter(order -> order.getOrderDate().isAfter(monthStart) &&
                                order.getOrderDate().isBefore(monthEnd))
                        .filter(order -> "DELIVERED".equals(order.getOrderStatus().toString()))
                        .collect(Collectors.toList());

                double revenue = monthOrders.stream()
                        .mapToDouble(order -> order.getTotalPrice().doubleValue())
                        .sum();

                int orderCount = monthOrders.size();

                reportData.add(AgencyReportDTO.builder()
                        .period(String.format("Tháng %d/%d", month, year))
                        .revenue(BigDecimal.valueOf(revenue))
                        .orderCount(orderCount)
                        .build());
            }
        }

        return reportData;
    }

    private List<AgencyReportDTO> generateMonthlyReport(List<OrderList> orders, int year, int month) {
        List<AgencyReportDTO> reportData = new ArrayList<>();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // Group by weeks in the month
        LocalDate currentWeekStart = startDate;
        int weekNumber = 1;

        while (currentWeekStart.isBefore(endDate) || currentWeekStart.isEqual(endDate)) {
            LocalDate currentWeekEnd = currentWeekStart.plusDays(6);
            if (currentWeekEnd.isAfter(endDate)) {
                currentWeekEnd = endDate;
            }

            LocalDateTime weekStart = currentWeekStart.atStartOfDay();
            LocalDateTime weekEnd = currentWeekEnd.atTime(23, 59, 59);

            List<OrderList> weekOrders = orders.stream()
                    .filter(order -> order.getOrderDate().isAfter(weekStart) &&
                            order.getOrderDate().isBefore(weekEnd))
                    .filter(order -> "DELIVERED".equals(order.getOrderStatus().toString()))
                    .collect(Collectors.toList());

            double revenue = weekOrders.stream()
                    .mapToDouble(order -> order.getTotalPrice().doubleValue())
                    .sum();

            int orderCount = weekOrders.size();

            reportData.add(AgencyReportDTO.builder()
                    .period(String.format("Tuần %d - %s/%d", weekNumber,
                            month < 10 ? "0" + month : String.valueOf(month), year))
                    .revenue(BigDecimal.valueOf(revenue))
                    .orderCount(orderCount)
                    .build());

            currentWeekStart = currentWeekStart.plusDays(7);
            weekNumber++;
        }

        return reportData;
    }

    private List<AgencyReportDTO> generateYearlyReport(List<OrderList> orders, int year) {
        List<AgencyReportDTO> reportData = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            LocalDateTime monthStart = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

            List<OrderList> monthOrders = orders.stream()
                    .filter(order -> order.getOrderDate().isAfter(monthStart) &&
                            order.getOrderDate().isBefore(monthEnd))
                    .filter(order -> "DELIVERED".equals(order.getOrderStatus().toString()))
                    .collect(Collectors.toList());

            double revenue = monthOrders.stream()
                    .mapToDouble(order -> order.getTotalPrice().doubleValue())
                    .sum();

            int orderCount = monthOrders.size();

            reportData.add(AgencyReportDTO.builder()
                    .period(String.format("Tháng %d/%d", month, year))
                    .revenue(BigDecimal.valueOf(revenue))
                    .orderCount(orderCount)
                    .build());
        }

        return reportData;
    }

    private ReportSummaryDTO calculateReportSummary(List<AgencyReportDTO> reportData, GenerateReportRequest request) {
        if (reportData.isEmpty()) {
            return ReportSummaryDTO.builder()
                    .totalRevenue(BigDecimal.ZERO)
                    .averageRevenue(BigDecimal.ZERO)
                    .totalOrders(0)
                    .growthRate(0.0)
                    .build();
        }

        double totalRevenue = reportData.stream()
                .mapToDouble(data -> data.getRevenue().doubleValue())
                .sum();

        double averageRevenue = totalRevenue / reportData.size();

        int totalOrders = reportData.stream()
                .mapToInt(AgencyReportDTO::getOrderCount)
                .sum();

        // Calculate growth rate comparing with previous period
        double growthRate = calculateGrowthRate(reportData, request);

        return ReportSummaryDTO.builder()
                .totalRevenue(BigDecimal.valueOf(totalRevenue))
                .averageRevenue(BigDecimal.valueOf(averageRevenue))
                .totalOrders(totalOrders)
                .growthRate(growthRate)
                .build();
    }

    private double calculateGrowthRate(List<AgencyReportDTO> reportData, GenerateReportRequest request) {
        if (reportData.size() < 2) {
            return 0.0;
        }

        // For growth calculation, compare current period with same period last year
        // This is a simplified implementation
        double currentPeriodRevenue = reportData.stream()
                .mapToDouble(data -> data.getRevenue().doubleValue())
                .sum();

        // Get previous year data for comparison
        GenerateReportRequest previousYearRequest = new GenerateReportRequest();
        previousYearRequest.setReportType(request.getReportType());
        previousYearRequest.setYear(request.getYear() - 1);
        previousYearRequest.setPeriod(request.getPeriod());

        try {
            Optional<Account> currentAgency = accountService.findCurrentUserInfo();
            if (currentAgency.isPresent()) {
                List<AgencyReportDTO> previousYearData = generateReportData(currentAgency.get(), previousYearRequest);
                double previousYearRevenue = previousYearData.stream()
                        .mapToDouble(data -> data.getRevenue().doubleValue())
                        .sum();

                if (previousYearRevenue == 0) {
                    return currentPeriodRevenue > 0 ? 100.0 : 0.0;
                }

                return ((currentPeriodRevenue - previousYearRevenue) / previousYearRevenue) * 100;
            }
        } catch (Exception e) {
            log.warn("Could not calculate growth rate", e);
        }

        return 0.0;
    }

    private RevenueTrendDTO calculateRevenueTrend(List<OrderList> orders, String period, int year) {
        List<TrendDataPoint> trendPoints = new ArrayList<>();

        if ("monthly".equals(period)) {
            for (int month = 1; month <= 12; month++) {
                LocalDateTime monthStart = LocalDateTime.of(year, month, 1, 0, 0);
                LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

                double revenue = calculateRevenue(orders, monthStart, monthEnd);

                trendPoints.add(TrendDataPoint.builder()
                        .label(String.format("T%d", month))
                        .value(BigDecimal.valueOf(revenue))
                        .build());
            }
        } else if ("daily".equals(period)) {
            // Last 30 days
            for (int day = 29; day >= 0; day--) {
                LocalDateTime dayStart = LocalDateTime.now().minusDays(day).toLocalDate().atStartOfDay();
                LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);

                double revenue = calculateRevenue(orders, dayStart, dayEnd);

                trendPoints.add(TrendDataPoint.builder()
                        .label(dayStart.format(DateTimeFormatter.ofPattern("dd/MM")))
                        .value(BigDecimal.valueOf(revenue))
                        .build());
            }
        }

        return RevenueTrendDTO.builder()
                .period(period)
                .year(year)
                .trendData(trendPoints)
                .build();
    }

    private ProductPerformanceDTO calculateProductPerformance(AgencyInfo agencyInfo, int year, int month) {
        List<Product> products = productRepository.findByAgencyInfo(agencyInfo);
        List<ProductPerformanceItem> performanceItems = new ArrayList<>();

        LocalDateTime monthStart = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

        for (Product product : products) {
            List<OrderDetail> orderDetails = orderDetailRepository.findAll().stream()
                    .filter(od -> od.getProduct().getProductId().equals(product.getProductId()))
                    .filter(od -> od.getOrderList().getOrderDate().isAfter(monthStart) &&
                            od.getOrderList().getOrderDate().isBefore(monthEnd))
                    .collect(Collectors.toList());

            int quantitySold = orderDetails.stream()
                    .mapToInt(OrderDetail::getQuantity)
                    .sum();

            BigDecimal revenue = orderDetails.stream()
                    .map(OrderDetail::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            performanceItems.add(ProductPerformanceItem.builder()
                    .productName(product.getProductName())
                    .quantitySold(quantitySold)
                    .revenue(revenue)
                    .build());
        }

        // Sort by revenue descending
        performanceItems.sort((a, b) -> b.getRevenue().compareTo(a.getRevenue()));

        return ProductPerformanceDTO.builder()
                .year(year)
                .month(month)
                .products(performanceItems)
                .build();
    }

    private OrderAnalyticsDTO calculateOrderAnalytics(List<OrderList> orders, String period, int year) {
        Map<String, Integer> statusCounts = new HashMap<>();
        Map<String, BigDecimal> statusRevenue = new HashMap<>();

        // Filter orders by period
        List<OrderList> filteredOrders = orders.stream()
                .filter(order -> order.getOrderDate().getYear() == year)
                .collect(Collectors.toList());

        // Count orders by status
        for (OrderList order : filteredOrders) {
            String status = order.getOrderStatus().toString();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            statusRevenue.put(status, statusRevenue.getOrDefault(status, BigDecimal.ZERO)
                    .add(order.getTotalPrice()));
        }

        List<OrderStatusAnalytics> statusAnalytics = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            statusAnalytics.add(OrderStatusAnalytics.builder()
                    .status(entry.getKey())
                    .count(entry.getValue())
                    .revenue(statusRevenue.get(entry.getKey()))
                    .build());
        }

        return OrderAnalyticsDTO.builder()
                .period(period)
                .year(year)
                .totalOrders(filteredOrders.size())
                .statusAnalytics(statusAnalytics)
                .build();
    }

    private byte[] generatePdfReport(List<AgencyReportDTO> reportData, ReportSummaryDTO summary,
                                     GenerateReportRequest request) throws Exception {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();

        // Add title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10);

        Paragraph title = new Paragraph("BÁO CÁO DOANH THU AGENCY", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        // Add report info
        document.add(new Paragraph("Loại báo cáo: " + request.getReportType().toUpperCase(), headerFont));
        document.add(new Paragraph("Năm: " + request.getYear(), headerFont));
        document.add(new Paragraph("Ngày tạo: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont));
        document.add(new Paragraph("\n"));

        // Add summary
        document.add(new Paragraph("TỔNG KẾT", headerFont));
        document.add(new Paragraph("Tổng doanh thu: " + String.format("%,.0f VNĐ", summary.getTotalRevenue()), normalFont));
        document.add(new Paragraph("Doanh thu trung bình: " + String.format("%,.0f VNĐ", summary.getAverageRevenue()), normalFont));
        document.add(new Paragraph("Tổng đơn hàng: " + summary.getTotalOrders(), normalFont));
        document.add(new Paragraph("Tỷ lệ tăng trưởng: " + String.format("%.2f%%", summary.getGrowthRate()), normalFont));
        document.add(new Paragraph("\n"));

        // Add data table
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{40f, 30f, 30f});

        // Table headers
        PdfPCell headerCell1 = new PdfPCell(new Phrase("Kỳ", headerFont));
        PdfPCell headerCell2 = new PdfPCell(new Phrase("Doanh thu (VNĐ)", headerFont));
        PdfPCell headerCell3 = new PdfPCell(new Phrase("Số đơn hàng", headerFont));

        headerCell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
        headerCell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
        headerCell3.setBackgroundColor(BaseColor.LIGHT_GRAY);

        table.addCell(headerCell1);
        table.addCell(headerCell2);
        table.addCell(headerCell3);

        // Table data
        for (AgencyReportDTO data : reportData) {
            table.addCell(new PdfPCell(new Phrase(data.getPeriod(), normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.format("%,.0f", data.getRevenue()), normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(data.getOrderCount()), normalFont)));
        }

        document.add(table);
        document.close();

        return baos.toByteArray();
    }

    private byte[] generateExcelReport(List<AgencyReportDTO> reportData, ReportSummaryDTO summary,
                                       GenerateReportRequest request) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Báo cáo doanh thu");

            // Create styles
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

            // Add title and info
            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("BÁO CÁO DOANH THU AGENCY");

            Row infoRow1 = sheet.createRow(2);
            infoRow1.createCell(0).setCellValue("Loại báo cáo:");
            infoRow1.createCell(1).setCellValue(request.getReportType().toUpperCase());

            Row infoRow2 = sheet.createRow(3);
            infoRow2.createCell(0).setCellValue("Năm:");
            infoRow2.createCell(1).setCellValue(request.getYear());

            Row infoRow3 = sheet.createRow(4);
            infoRow3.createCell(0).setCellValue("Ngày tạo:");
            infoRow3.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            // Add summary section
            Row summaryHeaderRow = sheet.createRow(6);
            summaryHeaderRow.createCell(0).setCellValue("TỔNG KẾT");

            Row summaryRow1 = sheet.createRow(7);
            summaryRow1.createCell(0).setCellValue("Tổng doanh thu:");
            Cell totalRevenueCell = summaryRow1.createCell(1);
            totalRevenueCell.setCellValue(summary.getTotalRevenue().doubleValue());
            totalRevenueCell.setCellStyle(currencyStyle);

            Row summaryRow2 = sheet.createRow(8);
            summaryRow2.createCell(0).setCellValue("Doanh thu trung bình:");
            Cell avgRevenueCell = summaryRow2.createCell(1);
            avgRevenueCell.setCellValue(summary.getAverageRevenue().doubleValue());
            avgRevenueCell.setCellStyle(currencyStyle);

            Row summaryRow3 = sheet.createRow(9);
            summaryRow3.createCell(0).setCellValue("Tổng đơn hàng:");
            summaryRow3.createCell(1).setCellValue(summary.getTotalOrders());

            Row summaryRow4 = sheet.createRow(10);
            summaryRow4.createCell(0).setCellValue("Tỷ lệ tăng trưởng:");
            summaryRow4.createCell(1).setCellValue(summary.getGrowthRate() + "%");

            // Create data table header
            Row headerRow = sheet.createRow(12);
            String[] headers = {"Kỳ", "Doanh thu (VNĐ)", "Số đơn hàng"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 13;
            for (AgencyReportDTO data : reportData) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(data.getPeriod());

                Cell revenueCell = row.createCell(1);
                revenueCell.setCellValue(data.getRevenue().doubleValue());
                revenueCell.setCellStyle(currencyStyle);

                row.createCell(2).setCellValue(data.getOrderCount());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private <T> ApiResponse<T> buildErrorResponse(int errorCode, String message) {
        return ApiResponse.<T>builder()
                .status(errorCode)
                .message(message)
                .timestamp(new Date())
                .build();
    }
}