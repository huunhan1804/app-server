// DashboardController.java
package com.example.shoppingsystem.controllers.web;

import com.example.shoppingsystem.services.interfaces.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("revenueData", adminDashboardService.getRevenueLast30Days());
        model.addAttribute("categoryStats", adminDashboardService.getCategoryStatistics());
        model.addAttribute("recentOrders", adminDashboardService.getRecentOrders());
        model.addAttribute("newUsers", adminDashboardService.getNewUsers());
        return "admin/dashboard";
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
}