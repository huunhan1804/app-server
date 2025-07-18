package com.example.shoppingsystem.services;

import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.OrderList;
import com.example.shoppingsystem.entities.Product;
import com.example.shoppingsystem.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AgencyInfoRepository agencyInfoRepository;

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private InsuranceClaimRepository insuranceClaimRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Thống kê tổng quan
        stats.put("totalUsers", accountRepository.count());
        stats.put("totalCustomers", accountRepository.countByRoleId(2L));
        stats.put("totalAgencies", accountRepository.countByRoleId(3L));
        stats.put("totalProducts", productRepository.count());
        stats.put("totalOrders", orderRepository.count());

        // Thống kê theo trạng thái
        stats.put("pendingAgencies", agencyInfoRepository.countByApprovalStatus("WAITING"));
        stats.put("pendingProducts", productRepository.countByApprovalStatus(Product.ApprovalStatus.PROCESSING));
        stats.put("activeProducts", productRepository.countByProductStatus(Product.ProductStatus.ACTIVE));
        stats.put("openTickets", supportTicketRepository.countOpenTickets());
        stats.put("pendingClaims", insuranceClaimRepository.countPendingClaims());

        // Thống kê doanh thu
        stats.put("totalRevenue", orderRepository.getTotalRevenue());
        stats.put("monthlyRevenue", orderRepository.getMonthlyRevenue());
        stats.put("todayOrders", orderRepository.getTodayOrdersCount());

        // Thống kê tăng trưởng
        stats.put("newUsersThisMonth", accountRepository.countNewUsersThisMonth());
        stats.put("ordersGrowth", orderRepository.getOrdersGrowthPercentage());
        stats.put("revenueGrowth", orderRepository.getRevenueGrowthPercentage());

        return stats;
    }

    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();

        // Recent activities
        overview.put("recentOrders", getRecentOrdersData());
        overview.put("recentUsers", getRecentUsersData());
        overview.put("pendingApprovals", agencyInfoRepository.getPendingApprovals());

        // Charts data
        overview.put("salesChart", getSalesChartData());
        overview.put("userGrowthChart", getUserGrowthChartData());
        overview.put("categoryDistribution", getCategoryDistributionData());

        return overview;
    }

    private List<Map<String, Object>> getRecentOrdersData() {
        List<OrderList> recentOrders = orderRepository.getRecentOrders(10);
        List<Map<String, Object>> orderData = new ArrayList<>();

        for (OrderList order : recentOrders) {
            Map<String, Object> data = new HashMap<>();
            data.put("orderCode", order.getOrderCode());
            data.put("customerName", order.getCustomerName());
            data.put("finalAmount", order.getFinalAmount());
            data.put("orderStatus", order.getOrderStatus());
            data.put("orderDate", order.getOrderDate());
            orderData.add(data);
        }

        return orderData;
    }

    private List<Map<String, Object>> getRecentUsersData() {
        List<Account> recentUsers = accountRepository.getRecentUsers(10);
        List<Map<String, Object>> userData = new ArrayList<>();

        for (Account user : recentUsers) {
            Map<String, Object> data = new HashMap<>();
            data.put("fullname", user.getFullname());
            data.put("email", user.getEmail());
            data.put("roleId", user.getRoleId());
            data.put("createdDate", user.getCreatedDate());
            userData.add(data);
        }

        return userData;
    }

    private List<Map<String, Object>> getSalesChartData() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        List<Map<String, Object>> chartData = new ArrayList<>();

        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dayData.put("sales", orderRepository.getSalesByDate(date));
            dayData.put("orders", orderRepository.getOrderCountByDate(date));
            chartData.add(dayData);
        }

        return chartData;
    }

    private List<Map<String, Object>> getUserGrowthChartData() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(12);

        List<Map<String, Object>> chartData = new ArrayList<>();

        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusMonths(1)) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", date.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            monthData.put("customers", accountRepository.getCustomerCountByMonth(date));
            monthData.put("agencies", accountRepository.getAgencyCountByMonth(date));
            chartData.add(monthData);
        }

        return chartData;
    }

    private List<Map<String, Object>> getCategoryDistributionData() {
        return productRepository.getCategoryDistribution();
    }
}
