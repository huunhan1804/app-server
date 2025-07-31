package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.dashboard.NewUserDTO;
import com.example.shoppingsystem.dtos.dashboard.RecentOrderDTO;
import com.example.shoppingsystem.services.interfaces.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class AdminApiController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/pending-counts")
    public ResponseEntity<Map<String, Integer>> getPendingCounts() {
        try {
            Map<String, Integer> counts = new HashMap<>();
            counts.put("pendingProducts", adminDashboardService.getPendingProductsCount());
            counts.put("pendingApplications", adminDashboardService.getPendingApplicationsCount());

            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            Map<String, Integer> errorCounts = new HashMap<>();
            errorCounts.put("pendingProducts", 0);
            errorCounts.put("pendingApplications", 0);
            return ResponseEntity.ok(errorCounts);
        }
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = adminDashboardService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("totalProducts", 0);
            errorStats.put("totalOrders", 0);
            errorStats.put("totalUsers", 0);
            errorStats.put("totalRevenue", "0 VND");
            return ResponseEntity.ok(errorStats);
        }
    }

    // Dashboard overview endpoints (limited data for cards)
    @GetMapping("/recent-orders")
    public ResponseEntity<Map<String, Object>> getRecentOrders() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("orders", adminDashboardService.getRecentOrders());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("orders", new java.util.ArrayList<>());
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading recent orders");
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/new-users")
    public ResponseEntity<Map<String, Object>> getNewUsers() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("users", adminDashboardService.getNewUsers());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("users", new java.util.ArrayList<>());
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading new users");
            return ResponseEntity.ok(errorResponse);
        }
    }

    // Paginated endpoints for detailed views
    @GetMapping("/recent-orders/paginated")
    public ResponseEntity<Map<String, Object>> getRecentOrdersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<RecentOrderDTO> ordersPage = adminDashboardService.getRecentOrders(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("content", ordersPage.getContent());
            response.put("totalElements", ordersPage.getTotalElements());
            response.put("totalPages", ordersPage.getTotalPages());
            response.put("currentPage", ordersPage.getNumber());
            response.put("size", ordersPage.getSize());
            response.put("numberOfElements", ordersPage.getNumberOfElements());
            response.put("first", ordersPage.isFirst());
            response.put("last", ordersPage.isLast());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("content", new java.util.ArrayList<>());
            errorResponse.put("totalElements", 0);
            errorResponse.put("totalPages", 0);
            errorResponse.put("currentPage", 0);
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading paginated orders");
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/new-users/paginated")
    public ResponseEntity<Map<String, Object>> getNewUsersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<NewUserDTO> usersPage = adminDashboardService.getNewUsers(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("content", usersPage.getContent());
            response.put("totalElements", usersPage.getTotalElements());
            response.put("totalPages", usersPage.getTotalPages());
            response.put("currentPage", usersPage.getNumber());
            response.put("size", usersPage.getSize());
            response.put("numberOfElements", usersPage.getNumberOfElements());
            response.put("first", usersPage.isFirst());
            response.put("last", usersPage.isLast());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("content", new java.util.ArrayList<>());
            errorResponse.put("totalElements", 0);
            errorResponse.put("totalPages", 0);
            errorResponse.put("currentPage", 0);
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading paginated users");
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/revenue-chart")
    public ResponseEntity<Map<String, Object>> getRevenueChart(
            @RequestParam(defaultValue = "30") int days) {
        try {
            Map<String, Object> chartData = adminDashboardService.getRevenueChartData(days);
            chartData.put("success", true);

            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("labels", new java.util.ArrayList<>());
            errorResponse.put("data", new java.util.ArrayList<>());
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading revenue chart");
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/category-chart")
    public ResponseEntity<Map<String, Object>> getCategoryChart() {
        try {
            Map<String, Object> chartData = adminDashboardService.getCategoryChartData();
            chartData.put("success", true);

            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("labels", new java.util.ArrayList<>());
            errorResponse.put("data", new java.util.ArrayList<>());
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading category chart");
            return ResponseEntity.ok(errorResponse);
        }
    }
}