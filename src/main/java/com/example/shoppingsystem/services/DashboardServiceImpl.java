package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.Regex;
import com.example.shoppingsystem.dtos.dashboard.*;
import com.example.shoppingsystem.entities.*;
import com.example.shoppingsystem.repositories.*;
import com.example.shoppingsystem.services.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<RevenueDataDTO> getRevenueLast30Days() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);

        List<OrderList> orders = orderRepository.findOrdersInDateRange(startDate, endDate);

        Map<String, BigDecimal> dailyRevenue = new LinkedHashMap<>();

        // Initialize all days with 0
        for (int i = 29; i >= 0; i--) {
            LocalDateTime date = endDate.minusDays(i);
            String dateKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            dailyRevenue.put(dateKey, BigDecimal.ZERO);
        }

        // Calculate actual revenue
        orders.stream()
                .filter(order -> "DELIVERED".equals(order.getOrderStatus().toString()))
                .forEach(order -> {
                    String dateKey = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    dailyRevenue.merge(dateKey, order.getTotalPrice(), BigDecimal::add);
                });

        return dailyRevenue.entrySet().stream()
                .map(entry -> new RevenueDataDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryStatDTO> getCategoryStatistics() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(category -> {
                    List<Product> products = productRepository.findByCategory_CategoryId(category.getCategoryId());
                    int productCount = products.size();
                    BigDecimal totalSales = products.stream()
                            .map(Product::getSoldAmount)
                            .map(BigDecimal::valueOf)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new CategoryStatDTO(
                            category.getCategoryName(),
                            productCount,
                            totalSales
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<RecentOrderDTO> getRecentOrders() {
        List<OrderList> recentOrders = orderRepository.findTop10ByOrderByOrderDateDesc();

        return recentOrders.stream()
                .map(order -> new RecentOrderDTO(
                        order.getOrderId(),
                        order.getAccount().getFullname(),
                        Regex.formatPriceToVND(order.getTotalPrice()),
                        order.getOrderStatus().toString(),
                        order.getOrderDate()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<NewUserDTO> getNewUsers() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Account> newUsers = accountRepository.findByCreatedDateAfter(sevenDaysAgo);

        return newUsers.stream()
                .map(account -> new NewUserDTO(
                        account.getFullname(),
                        account.getEmail(),
                        account.getRole().getRoleName(),
                        account.getCreatedDate()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getRevenueChartData(int days) {
        List<RevenueDataDTO> revenueData = getRevenueLast30Days();

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", revenueData.stream()
                .map(RevenueDataDTO::getDate)
                .collect(Collectors.toList()));
        chartData.put("data", revenueData.stream()
                .map(RevenueDataDTO::getRevenue)
                .collect(Collectors.toList()));

        return chartData;
    }

    @Override
    public Map<String, Object> getCategoryChartData() {
        List<CategoryStatDTO> categoryStats = getCategoryStatistics();

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", categoryStats.stream()
                .map(CategoryStatDTO::getCategoryName)
                .collect(Collectors.toList()));
        chartData.put("data", categoryStats.stream()
                .map(CategoryStatDTO::getProductCount)
                .collect(Collectors.toList()));

        return chartData;
    }
}