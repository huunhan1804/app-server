package com.example.shoppingsystem.api;

import com.example.shoppingsystem.services.SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/support")
@CrossOrigin(origins = "*")
public class SupportController {

    @Autowired
    private SupportService supportService;

    @GetMapping("/tickets")
    public ResponseEntity<Map<String, Object>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Map<String, Object> result = supportService.getAllTickets(pageable, status, priority);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/insurance-claims")
    public ResponseEntity<Map<String, Object>> getInsuranceClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Map<String, Object> result = supportService.getInsuranceClaims(pageable, status);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/insurance-claims/{claimId}")
    public ResponseEntity<Map<String, Object>> getClaimDetail(@PathVariable Long claimId) {
        try {
            Map<String, Object> claimDetail = supportService.getClaimDetail(claimId);
            return ResponseEntity.ok(claimDetail);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/insurance-claims/{claimId}/approve")
    public ResponseEntity<Map<String, Object>> approveClaim(
            @PathVariable Long claimId,
            @RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = supportService.approveClaim(claimId, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/insurance-claims/{claimId}/reject")
    public ResponseEntity<Map<String, Object>> rejectClaim(
            @PathVariable Long claimId,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            Map<String, Object> result = supportService.rejectClaim(claimId, reason);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
