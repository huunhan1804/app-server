package com.example.shoppingsystem.api;

import com.example.shoppingsystem.services.ProductManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = "*")
public class ProductManagementController {

    @Autowired
    private ProductManagementService productManagementService;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long agencyId) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Map<String, Object> result = productManagementService.getAllProducts(
                    pageable, search, status, categoryId, agencyId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingProducts() {
        try {
            Map<String, Object> result = productManagementService.getPendingProducts();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{productId}/approve")
    public ResponseEntity<Map<String, Object>> approveProduct(@PathVariable Long productId) {
        try {
            Map<String, Object> result = productManagementService.approveProduct(productId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{productId}/reject")
    public ResponseEntity<Map<String, Object>> rejectProduct(
            @PathVariable Long productId,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            Map<String, Object> result = productManagementService.rejectProduct(productId, reason);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getProductDetail(@PathVariable Long productId) {
        try {
            Map<String, Object> productDetail = productManagementService.getProductDetail(productId);
            return ResponseEntity.ok(productDetail);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long productId) {
        try {
            Map<String, Object> result = productManagementService.deleteProduct(productId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
