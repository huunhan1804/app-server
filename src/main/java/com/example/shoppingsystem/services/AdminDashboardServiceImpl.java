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
        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(30);

            List<com.example.shoppingsystem.entities.OrderList> orders =
                    orderRepository.findOrdersInDateRange(startDate, endDate);

            // Group orders by date and sum revenue
            Map<LocalDate, BigDecimal> dailyRevenue = orders.stream()
                    .collect(Collectors.groupingBy(
                            order -> order.getOrderDate().toLocalDate(),
                            Collectors.reducing(BigDecimal.ZERO,
                                    com.example.shoppingsystem.entities.OrderList::getTotalPrice,
                                    BigDecimal::add)
                    ));

            List<RevenueDataDTO> result = new ArrayList<>();
            LocalDate currentDate = startDate.toLocalDate();

            while (!currentDate.isAfter(endDate.toLocalDate())) {
                BigDecimal revenue = dailyRevenue.getOrDefault(currentDate, BigDecimal.ZERO);
                result.add(new RevenueDataDTO(
                        currentDate.format(DateTimeFormatter.ofPattern("dd/MM")),
                        revenue
                ));
                currentDate = currentDate.plusDays(1);
            }

            return result;
        } catch (Exception e) {
            log.error("Error getting revenue data for last 30 days", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<CategoryStatDTO> getCategoryStatistics() {
        try {
            List<com.example.shoppingsystem.entities.Category> categories = categoryRepository.findAll();
            List<CategoryStatDTO> stats = new ArrayList<>();

            for (com.example.shoppingsystem.entities.Category category : categories) {
                long productCount = productRepository.findByCategory_CategoryId(category.getCategoryId()).size();

                // Calculate total sales for this category
                BigDecimal totalSales = orderDetailRepository.findAll().stream()
                        .filter(od -> od.getProduct() != null &&
                                od.getProduct().getCategory().getCategoryId().equals(category.getCategoryId()))
                        .map(com.example.shoppingsystem.entities.OrderDetail::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                stats.add(new CategoryStatDTO(
                        category.getCategoryName(),
                        (int) productCount,
                        totalSales
                ));
            }

            return stats.stream()
                    .sorted((a, b) -> b.getTotalSales().compareTo(a.getTotalSales()))
                    .limit(10)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting category statistics", e);
            return new ArrayList<>();
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
            return (int) productRepository.findByApprovalStatus_StatusCode("pending").size();
        } catch (Exception e) {
            log.error("Error getting pending products count", e);
            return 0;
        }
    }

    @Override
    public int getPendingApplicationsCount() {
        try {
            return (int) agencyInfoRepository.findByApprovalStatus_StatusCode("pending")
                    .map(page -> 1)
                    .orElse(0);
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

    @Override
    public String getTotalRevenue() {
        try {
            BigDecimal totalRevenue = orderRepository.findAll().stream()
                    .map(com.example.shoppingsystem.entities.OrderList::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return formatCurrency(totalRevenue);
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