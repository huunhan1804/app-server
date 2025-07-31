package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.dashboard.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public interface AdminDashboardService {
    // Dashboard methods
    List<RevenueDataDTO> getRevenueLast30Days();
    List<CategoryStatDTO> getCategoryStatistics();

    // Pagination methods for tables
    Page<RecentOrderDTO> getRecentOrders(Pageable pageable);
    Page<NewUserDTO> getNewUsers(Pageable pageable);

    // Quick access methods (for dashboard overview)
    List<RecentOrderDTO> getRecentOrders(); // Top 5 for dashboard
    List<NewUserDTO> getNewUsers(); // Top 5 for dashboard

    Map<String, Object> getRevenueChartData(int days);
    Map<String, Object> getCategoryChartData();

    // Admin specific methods
    int getPendingProductsCount();
    int getPendingApplicationsCount();

    // Dashboard stats
    Map<String, Object> getDashboardStats();
    int getTotalProducts();
    int getTotalOrders();
    int getTotalUsers();
    String getTotalRevenue();
}