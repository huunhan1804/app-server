package com.example.shoppingsystem.api;

import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.services.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class UserManagementController {

    @Autowired
    private UserManagementService userManagementService;

    @GetMapping("/customers")
    public ResponseEntity<Page<Account>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Account> customers = userManagementService.getCustomers(pageable, search);
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/agencies")
    public ResponseEntity<Map<String, Object>> getAgencies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Map<String, Object> result = userManagementService.getAgencies(pageable, search, status);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/agencies/pending")
    public ResponseEntity<Map<String, Object>> getPendingAgencies() {
        try {
            Map<String, Object> result = userManagementService.getPendingAgencies();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/agencies/{agencyId}/approve")
    public ResponseEntity<Map<String, Object>> approveAgency(@PathVariable Long agencyId) {
        try {
            Map<String, Object> result = userManagementService.approveAgency(agencyId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/agencies/{agencyId}/reject")
    public ResponseEntity<Map<String, Object>> rejectAgency(
            @PathVariable Long agencyId,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            Map<String, Object> result = userManagementService.rejectAgency(agencyId, reason);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable Long userId) {
        try {
            Map<String, Object> userDetail = userManagementService.getUserDetail(userId);
            return ResponseEntity.ok(userDetail);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            Map<String, Object> result = userManagementService.updateUserStatus(userId, status);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
