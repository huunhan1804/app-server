package com.example.shoppingsystem.services;

import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.ApprovalStatus;
import com.example.shoppingsystem.entities.OrderList;
import com.example.shoppingsystem.entities.Product;
import com.example.shoppingsystem.enums.ProductStatus;
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

    @Autowired
    private ApprovalStatusRepository approvalStatusRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Thống kê tổng quan
        stats.put("totalUsers", accountRepository.count());
        stats.put("totalCustomers", accountRepository.countByRoleId(2L));
        stats.put("totalAgencies", accountRepository.countByRoleId(3L));
        stats.put("totalProducts", productRepository.count());
        stats.put("totalOrders", orderRepository.count());

        // Thống kê theo trạng thái - Sửa lỗi
        ApprovalStatus processingStatus = approvalStatusRepository.findApprovalStatusByStatusCode("PROCESSING");
        ApprovalStatus waitingStatus = approvalStatusRepository.findApprovalStatusByStatusCode("WAITING");

        stats.put("pendingAgencies", agencyInfoRepository.countByApprovalStatus("WAITING"));
        stats.put("pendingProducts", productRepository.countByApprovalStatus(processingStatus));
        stats.put("activeProducts", productRepository.countByProductStatus(ProductStatus.ACTIVE));
        stats.put("openTickets", supportTicketRepository.countOpenTickets());
        stats.put("pendingClaims", insuranceClaimRepository.countPendingClaims());

        // Thống kê doanh thu
        stats.put("totalRevenue", orderRepository.getMonthlyRevenue());
        stats.put("monthlyRevenue", orderRepository.getMonthlyRevenue());
        stats.put("todayOrders", orderRepository.getTodayOrdersCount());

        // Thống kê tăng trưởng
        stats.put("newUsersThisMonth", accountRepository.countNewUsersThisMonth());
        stats.put("ordersGrowth", orderRepository.getOrdersGrowthPercentage());
        stats.put("revenueGrowth", orderRepository.getRevenueGrowthPercentage());

        return stats;
    }

    // Các method khác giữ nguyên...
}