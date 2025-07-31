package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.AgencyApplicationDetailDTO;
import com.example.shoppingsystem.dtos.UserManagementDTO;
import com.example.shoppingsystem.services.interfaces.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class AdminUserApiController {

    private final UserManagementService userManagementService;

    // ==================== CUSTOMER MANAGEMENT ====================

    @GetMapping("/customers")
    public ResponseEntity<Map<String, Object>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String accountStatus,
            @RequestParam(required = false) String membershipLevel,
            @RequestParam(required = false) String keyword) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            Page<UserManagementDTO> customers = userManagementService.getCustomers(
                    pageable, accountStatus, membershipLevel, keyword);

            Map<String, Object> response = new HashMap<>();
            response.put("customers", customers.getContent());
            response.put("currentPage", customers.getNumber());
            response.put("totalPages", customers.getTotalPages());
            response.put("totalElements", customers.getTotalElements());
            response.put("hasNext", customers.hasNext());
            response.put("hasPrevious", customers.hasPrevious());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading customers: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/customers/filters")
    public ResponseEntity<Map<String, Object>> getCustomerFilters() {
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("accountStatuses", userManagementService.getAllAccountStatuses());
            filters.put("membershipLevels", userManagementService.getAllMembershipLevels());
            filters.put("success", true);

            return ResponseEntity.ok(filters);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading filters");
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/customers/{id}/suspend")
    public ResponseEntity<Map<String, Object>> suspendCustomer(
            @PathVariable Long id,
            @RequestParam String reason) {
        try {
            userManagementService.suspendCustomer(id, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Customer suspended successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error suspending customer: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/customers/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateCustomer(@PathVariable Long id) {
        try {
            userManagementService.activateCustomer(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Customer activated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error activating customer: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/customers/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetCustomerPassword(@PathVariable Long id) {
        try {
            userManagementService.resetCustomerPassword(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error resetting password: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    // ==================== AGENCY MANAGEMENT ====================

    @GetMapping("/agencies")
    public ResponseEntity<Map<String, Object>> getAllAgencies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) String keyword) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            Page<UserManagementDTO> agencies = userManagementService.getAgencies(
                    pageable, approvalStatus, keyword);

            Map<String, Object> response = new HashMap<>();
            response.put("agencies", agencies.getContent());
            response.put("currentPage", agencies.getNumber());
            response.put("totalPages", agencies.getTotalPages());
            response.put("totalElements", agencies.getTotalElements());
            response.put("hasNext", agencies.hasNext());
            response.put("hasPrevious", agencies.hasPrevious());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading agencies: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/agencies/filters")
    public ResponseEntity<Map<String, Object>> getAgencyFilters() {
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("approvalStatuses", userManagementService.getAllApprovalStatuses());
            filters.put("success", true);

            return ResponseEntity.ok(filters);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading filters");
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/agencies/{id}/suspend")
    public ResponseEntity<Map<String, Object>> suspendAgency(
            @PathVariable Long id,
            @RequestParam String reason) {
        try {
            userManagementService.suspendAgency(id, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Agency suspended successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error suspending agency: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/agencies/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateAgency(@PathVariable Long id) {
        try {
            userManagementService.activateAgency(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Agency activated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error activating agency: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    // ==================== AGENCY APPLICATIONS ====================

    @GetMapping("/agencies/applications")
    public ResponseEntity<Map<String, Object>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            Page<AgencyApplicationDetailDTO> applications = userManagementService.getPendingApplications(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("applications", applications.getContent());
            response.put("currentPage", applications.getNumber());
            response.put("totalPages", applications.getTotalPages());
            response.put("totalElements", applications.getTotalElements());
            response.put("hasNext", applications.hasNext());
            response.put("hasPrevious", applications.hasPrevious());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading applications: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/agencies/applications/{id}")
    public ResponseEntity<Map<String, Object>> getApplicationDetail(@PathVariable Long id) {
        try {
            AgencyApplicationDetailDTO application = userManagementService.getApplicationDetail(id);

            Map<String, Object> response = new HashMap<>();
            response.put("application", application);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading application detail: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    // AdminUserApiController.java - Updated methods
    @PostMapping("/agencies/applications/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveApplication(@PathVariable Long id) {
        try {
            String adminUsername = getCurrentAdminUsername();
            userManagementService.approveApplication(id, adminUsername);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Application approved successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error approving application: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/agencies/applications/{id}/decline")
    public ResponseEntity<Map<String, Object>> declineApplication(
            @PathVariable Long id,
            @RequestParam String reason) {
        try {
            String adminUsername = getCurrentAdminUsername();
            userManagementService.declineApplication(id, reason, adminUsername);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Application declined successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error declining application: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    // Helper method
    private String getCurrentAdminUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "System";
        }
    }
}