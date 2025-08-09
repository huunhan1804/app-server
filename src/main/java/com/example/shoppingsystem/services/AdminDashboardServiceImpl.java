package com.example.shoppingsystem.services;

import com.example.shoppingsystem.dtos.dashboard.*;
import com.example.shoppingsystem.repositories.*;
import com.example.shoppingsystem.services.interfaces.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final AgencyInfoRepository agencyInfoRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<RevenueDataDTO> getRevenueLast30Days() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(30);
        var rows = orderRepository.sumRevenueByDate(start, end); // ít row, nhẹ network
        Map<LocalDate, BigDecimal> map = new HashMap<>();
        for (Object[] r : rows) map.put(((java.sql.Date) r[0]).toLocalDate(), (BigDecimal) r[1]);

        List<RevenueDataDTO> result = new ArrayList<>();
        for (LocalDate d = start.toLocalDate(); !d.isAfter(end.toLocalDate()); d = d.plusDays(1)) {
            result.add(new RevenueDataDTO(d.format(DateTimeFormatter.ofPattern("dd/MM")), map.getOrDefault(d, BigDecimal.ZERO)));
        }
        return result;
    }

    @Override
    public List<CategoryStatDTO> getCategoryStatistics() {
        try {
            var rows = categoryRepository.getCategoryStats();
            return rows.stream()
                    .map(r -> new CategoryStatDTO(
                            r.getCategoryName(),
                            r.getProductCount() == null ? 0 : r.getProductCount().intValue(),
                            r.getTotalSales() == null ? BigDecimal.ZERO : r.getTotalSales()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting category stats", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Page<RecentOrderDTO> getRecentOrders(Pageable pageable) {
        try {
            Page<com.example.shoppingsystem.entities.OrderList> orderPage =
                    orderRepository.findAllByOrderByOrderDateDesc(pageable);

            List<RecentOrderDTO> recentOrderDTOs = orderPage.getContent().stream()
                    .map(order -> new RecentOrderDTO(
                            order.getOrderId(),
                            order.getAccount().getFullname(),
                            formatCurrency(order.getTotalPrice()),
                            translateOrderStatus(order.getOrderStatus().toString()),
                            order.getOrderDate()
                    ))
                    .collect(Collectors.toList());

            return new PageImpl<>(recentOrderDTOs, pageable, orderPage.getTotalElements());
        } catch (Exception e) {
            log.error("Error getting paginated recent orders", e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    @Override
    public List<RecentOrderDTO> getRecentOrders() {
        try {
            List<com.example.shoppingsystem.entities.OrderList> recentOrders =
                    orderRepository.findTop5ByOrderByOrderDateDesc();

            return recentOrders.stream()
                    .map(order -> new RecentOrderDTO(
                            order.getOrderId(),
                            order.getAccount().getFullname(),
                            formatCurrency(order.getTotalPrice()),
                            translateOrderStatus(order.getOrderStatus().toString()),
                            order.getOrderDate()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting recent orders for dashboard", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Page<NewUserDTO> getNewUsers(Pageable pageable) {
        try {
            // Get users from last 30 days for more data
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            Page<com.example.shoppingsystem.entities.Account> userPage =
                    accountRepository.findByCreatedDateAfterOrderByCreatedDateDesc(thirtyDaysAgo, pageable);

            List<NewUserDTO> newUserDTOs = userPage.getContent().stream()
                    .map(user -> new NewUserDTO(
                            user.getFullname(),
                            user.getEmail(),
                            translateRole(user.getRole().getRoleName()),
                            user.getCreatedDate()
                    ))
                    .collect(Collectors.toList());

            return new PageImpl<>(newUserDTOs, pageable, userPage.getTotalElements());
        } catch (Exception e) {
            log.error("Error getting paginated new users", e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    @Override
    public List<NewUserDTO> getNewUsers() {
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<com.example.shoppingsystem.entities.Account> newUsers =
                    accountRepository.findTop5ByCreatedDateAfterOrderByCreatedDateDesc(sevenDaysAgo);

            return newUsers.stream()
                    .map(user -> new NewUserDTO(
                            user.getFullname(),
                            user.getEmail(),
                            translateRole(user.getRole().getRoleName()),
                            user.getCreatedDate()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting new users for dashboard", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getRevenueChartData(int days) {
        try {
            List<RevenueDataDTO> revenueData = getRevenueLast30Days();

            Map<String, Object> chartData = new HashMap<>();
            chartData.put("labels", revenueData.stream()
                    .map(RevenueDataDTO::getDate)
                    .collect(Collectors.toList()));
            chartData.put("data", revenueData.stream()
                    .map(RevenueDataDTO::getRevenue)
                    .collect(Collectors.toList()));

            return chartData;
        } catch (Exception e) {
            log.error("Error getting revenue chart data", e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> getCategoryChartData() {
        try {
            List<CategoryStatDTO> categoryStats = getCategoryStatistics();

            Map<String, Object> chartData = new HashMap<>();
            chartData.put("labels", categoryStats.stream()
                    .map(CategoryStatDTO::getCategoryName)
                    .collect(Collectors.toList()));
            chartData.put("data", categoryStats.stream()
                    .map(CategoryStatDTO::getProductCount)
                    .collect(Collectors.toList()));

            return chartData;
        } catch (Exception e) {
            log.error("Error getting category chart data", e);
            return new HashMap<>();
        }
    }

    @Override
    public int getPendingProductsCount() {
        try {
            return (int) productRepository.countByApprovalStatus_StatusCode("pending");
        } catch (Exception e) {
            log.error("Error getting pending products count", e);
            return 0;
        }
    }

    @Override
    public int getPendingApplicationsCount() {
        try {
            return (int) agencyInfoRepository.countByApprovalStatus_StatusCode("pending");
        } catch (Exception e) {
            log.error("Error getting pending applications count", e);
            return 0;
        }
    }


    @Override
    public Map<String, Object> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            stats.put("totalProducts", getTotalProducts());
            stats.put("totalOrders", getTotalOrders());
            stats.put("totalUsers", getTotalUsers());
            stats.put("totalRevenue", getTotalRevenue());

            return stats;
        } catch (Exception e) {
            log.error("Error getting dashboard stats", e);
            return new HashMap<>();
        }
    }

    @Override
    public int getTotalProducts() {
        try {
            return (int) productRepository.count();
        } catch (Exception e) {
            log.error("Error getting total products", e);
            return 0;
        }
    }

    @Override
    public int getTotalOrders() {
        try {
            return (int) orderRepository.count();
        } catch (Exception e) {
            log.error("Error getting total orders", e);
            return 0;
        }
    }

    @Override
    public int getTotalUsers() {
        try {
            return (int) accountRepository.count();
        } catch (Exception e) {
            log.error("Error getting total users", e);
            return 0;
        }
    }

    // AdminDashboardServiceImpl.java
    @Override
    public String getTotalRevenue() {
        try {
            BigDecimal total = orderRepository.sumAllRevenue();
            return formatCurrency(total);
        } catch (Exception e) {
            log.error("Error getting total revenue", e);
            return "0 VND";
        }
    }


    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 VND";
        }
        return String.format("%,.0f VND", amount);
    }

    private String translateOrderStatus(String status) {
        switch (status.toLowerCase()) {
            case "pending": return "Chờ xử lý";
            case "confirmed": return "Đã xác nhận";
            case "shipping": return "Đang giao";
            case "delivered": return "Đã giao";
            case "cancelled": return "Đã hủy";
            default: return status;
        }
    }

    private String translateRole(String role) {
        switch (role.toLowerCase()) {
            case "admin":
            case "administrator": return "Quản trị viên";
            case "agency": return "Đại lý";
            case "customer": return "Khách hàng";
            default: return role;
        }
    }
}