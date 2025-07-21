// DashboardController.java
package com.example.shoppingsystem.controllers.web;

import com.example.shoppingsystem.services.interfaces.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Load dữ liệu cho dashboard
        model.addAttribute("revenueData", adminDashboardService.getRevenueLast30Days());
        model.addAttribute("categoryStats", adminDashboardService.getCategoryStatistics());
        model.addAttribute("recentOrders", adminDashboardService.getRecentOrders());
        model.addAttribute("newUsers", adminDashboardService.getNewUsers());
        return "admin/dashboard";
    }

    // API endpoints cho AJAX calls
    @GetMapping("/api/pending-counts")
    @ResponseBody
    public Map<String, Integer> getPendingCounts() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("pendingProducts", adminDashboardService.getPendingProductsCount());
        counts.put("pendingApplications", adminDashboardService.getPendingApplicationsCount());
        return counts;
    }

    @GetMapping("/api/dashboard-stats")
    @ResponseBody
    public Map<String, Object> getDashboardStats() {
        return adminDashboardService.getDashboardStats();
    }

    @GetMapping("/revenue-chart")
    @ResponseBody
    public Map<String, Object> getRevenueChart(@RequestParam(defaultValue = "30") int days) {
        return adminDashboardService.getRevenueChartData(days);
    }

    @GetMapping("/category-chart")
    @ResponseBody
    public Map<String, Object> getCategoryChart() {
        return adminDashboardService.getCategoryChartData();
    }

    @GetMapping("/api/recent-orders")
    @ResponseBody
    public Map<String, Object> getRecentOrders() {
        Map<String, Object> response = new HashMap<>();
        response.put("orders", adminDashboardService.getRecentOrders());
        return response;
    }

    @GetMapping("/api/new-users")
    @ResponseBody
    public Map<String, Object> getNewUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("users", adminDashboardService.getNewUsers());
        return response;
    }
}