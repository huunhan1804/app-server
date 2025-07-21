// AdminDashboardService.java
package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.dashboard.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public interface AdminDashboardService {
    // Dashboard methods
    List<RevenueDataDTO> getRevenueLast30Days();
    List<CategoryStatDTO> getCategoryStatistics();
    List<RecentOrderDTO> getRecentOrders();
    List<NewUserDTO> getNewUsers();
    Map<String, Object> getRevenueChartData(int days);
    Map<String, Object> getCategoryChartData();

    // Admin specific methods
    int getPendingProductsCount();
    int getPendingApplicationsCount();

    // New methods for dashboard stats
    Map<String, Object> getDashboardStats();
    int getTotalProducts();
    int getTotalOrders();
    int getTotalUsers();
    String getTotalRevenue();
}