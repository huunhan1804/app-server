// AdminInsuranceApiController.java
package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.InsuranceClaimDTO;
import com.example.shoppingsystem.entities.InsuranceClaim.ClaimStatus;
import com.example.shoppingsystem.requests.ProcessInsuranceClaimRequest;
import com.example.shoppingsystem.requests.SendClaimCommunicationRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.InsuranceClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/insurance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class AdminInsuranceApiController {

    private final InsuranceClaimService insuranceClaimService;

    // ==================== CLAIM MANAGEMENT ====================

    @GetMapping("/claims")
    public ResponseEntity<Map<String, Object>> getAllClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            ClaimStatus claimStatus = status != null && !status.isEmpty()
                    ? ClaimStatus.valueOf(status)
                    : null;

            ApiResponse<Page<InsuranceClaimDTO>> response =
                    insuranceClaimService.getAllClaims(pageable, claimStatus, keyword);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", response.getMessage());
            result.put("data", response.getData());
            result.put("currentPage", page);
            result.put("totalPages", response.getData().getTotalPages());
            result.put("totalElements", response.getData().getTotalElements());
            result.put("pageSize", size);
            result.put("sortBy", sortBy);
            result.put("sortDir", sortDir);
            result.put("status", status);
            result.put("keyword", keyword);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Có lỗi xảy ra khi tải danh sách yêu cầu bảo hiểm: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/claims/{claimId}")
    public ResponseEntity<Map<String, Object>> getClaimDetail(@PathVariable Long claimId) {
        try {
            ApiResponse<InsuranceClaimDTO> response = insuranceClaimService.getClaimDetail(claimId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", response.getMessage());
            result.put("data", response.getData());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Có lỗi xảy ra khi tải chi tiết yêu cầu: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/claims/{claimId}/process")
    public ResponseEntity<Map<String, Object>> processClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody ProcessInsuranceClaimRequest request) {
        try {
            ApiResponse<InsuranceClaimDTO> response =
                    insuranceClaimService.updateClaimStatus(claimId, request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", response.getMessage());
            result.put("data", response.getData());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Có lỗi xảy ra khi xử lý yêu cầu: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/communication")
    public ResponseEntity<Map<String, Object>> sendCommunication(
            @Valid @RequestBody SendClaimCommunicationRequest request) {
        try {
            ApiResponse<Void> response = insuranceClaimService.sendCommunication(request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", response.getMessage());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Có lỗi xảy ra khi gửi email: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== STATISTICS & COUNTS ====================

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getInsuranceStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // Get counts for each status
            for (ClaimStatus status : ClaimStatus.values()) {
                ApiResponse<Page<InsuranceClaimDTO>> response =
                        insuranceClaimService.getAllClaims(
                                PageRequest.of(0, 1), status, null);
                stats.put(status.name().toLowerCase() + "Count",
                        response.getData().getTotalElements());
            }

            // Total claims
            ApiResponse<Page<InsuranceClaimDTO>> totalResponse =
                    insuranceClaimService.getAllClaims(
                            PageRequest.of(0, 1), null, null);
            stats.put("totalClaims", totalResponse.getData().getTotalElements());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", stats);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Có lỗi xảy ra khi tải thống kê: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/statuses")
    public ResponseEntity<Map<String, Object>> getClaimStatuses() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", ClaimStatus.values());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== CUSTOMER & AGENCY CLAIMS ====================

    @GetMapping("/customer/{customerId}/claims")
    public ResponseEntity<Map<String, Object>> getClaimsByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size,
                    Sort.by("submittedDate").descending());

            ApiResponse<Page<InsuranceClaimDTO>> response =
                    insuranceClaimService.getClaimsByCustomer(customerId, pageable);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", response.getMessage());
            result.put("data", response.getData());
            result.put("currentPage", page);
            result.put("totalPages", response.getData().getTotalPages());
            result.put("totalElements", response.getData().getTotalElements());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/agency/{agencyId}/claims")
    public ResponseEntity<Map<String, Object>> getClaimsByAgency(
            @PathVariable Long agencyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size,
                    Sort.by("submittedDate").descending());

            ApiResponse<Page<InsuranceClaimDTO>> response =
                    insuranceClaimService.getClaimsByAgency(agencyId, pageable);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", response.getMessage());
            result.put("data", response.getData());
            result.put("currentPage", page);
            result.put("totalPages", response.getData().getTotalPages());
            result.put("totalElements", response.getData().getTotalElements());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== REPORTS ====================

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getInsuranceReports(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String reportType) {
        try {
            // TODO: Implement report generation logic
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("message", "Reports feature coming soon");

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", reportData);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Có lỗi xảy ra khi tạo báo cáo: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}