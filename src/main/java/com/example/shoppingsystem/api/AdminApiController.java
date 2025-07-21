// AdminApiController.java
package com.example.shoppingsystem.api;

import com.example.shoppingsystem.services.interfaces.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminApiController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/pending-counts")
    public Map<String, Integer> getPendingCounts() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("pendingProducts", adminDashboardService.getPendingProductsCount());
        counts.put("pendingApplications", adminDashboardService.getPendingApplicationsCount());
        return counts;
    }
}