package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.dashboard.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public interface DashboardService {
    List<RevenueDataDTO> getRevenueLast30Days();
    List<CategoryStatDTO> getCategoryStatistics();
    List<RecentOrderDTO> getRecentOrders();
    List<NewUserDTO> getNewUsers();
    Map<String, Object> getRevenueChartData(int days);
    Map<String, Object> getCategoryChartData();
}