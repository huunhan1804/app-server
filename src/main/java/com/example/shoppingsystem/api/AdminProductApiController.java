package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.ProductManagementDTO;
import com.example.shoppingsystem.services.interfaces.ProductManagementService;
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
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class AdminProductApiController {

    private final ProductManagementService productManagementService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,      // ← Đổi từ String category
            @RequestParam(required = false) Long agencyId,        // ← Đổi từ String agency
            @RequestParam(required = false) String keyword) {
        try {
            // Debug log
            System.out.println("Filter params received:");
            System.out.println("Status: " + status);
            System.out.println("CategoryId: " + categoryId);
            System.out.println("AgencyId: " + agencyId);
            System.out.println("Keyword: " + keyword);
            System.out.println("Page: " + page + ", Size: " + size);

            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            Page<ProductManagementDTO> products = productManagementService.getAllProducts(
                    pageable, status, categoryId, agencyId, keyword);

            System.out.println("Total products found: " + products.getTotalElements());

            Map<String, Object> response = new HashMap<>();
            response.put("content", products.getContent());
            response.put("totalElements", products.getTotalElements());
            response.put("totalPages", products.getTotalPages());
            response.put("currentPage", products.getNumber());
            response.put("size", products.getSize());
            response.put("numberOfElements", products.getNumberOfElements());
            response.put("first", products.isFirst());
            response.put("last", products.isLast());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading products: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
            Page<ProductManagementDTO> pendingProducts = productManagementService.getPendingProducts(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("content", pendingProducts.getContent());
            response.put("totalElements", pendingProducts.getTotalElements());
            response.put("totalPages", pendingProducts.getTotalPages());
            response.put("currentPage", pendingProducts.getNumber());
            response.put("size", pendingProducts.getSize());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading pending products: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getProductForReview(@PathVariable Long productId) {
        try {
            ProductManagementDTO product = productManagementService.getProductForReview(productId);

            Map<String, Object> response = new HashMap<>();
            response.put("product", product);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading product: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/{productId}/approve")
    public ResponseEntity<Map<String, Object>> approveProduct(@PathVariable Long productId) {
        try {
            productManagementService.approveProduct(productId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sản phẩm đã được phê duyệt thành công!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/{productId}/reject")
    public ResponseEntity<Map<String, Object>> rejectProduct(
            @PathVariable Long productId,
            @RequestBody Map<String, String> request) {
        try {
            String rejectionReason = request.get("rejectionReason");
            productManagementService.rejectProduct(productId, rejectionReason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sản phẩm đã bị từ chối!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/{productId}/request-edit")
    public ResponseEntity<Map<String, Object>> requestEdit(
            @PathVariable Long productId,
            @RequestBody Map<String, String> request) {
        try {
            String editNotes = request.get("editNotes");
            productManagementService.requestProductEdit(productId, editNotes);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã gửi yêu cầu chỉnh sửa đến Agency!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/{productId}/remove")
    public ResponseEntity<Map<String, Object>> removeProduct(
            @PathVariable Long productId,
            @RequestBody Map<String, String> request) {
        try {
            String removeReason = request.get("removeReason");
            productManagementService.removeProduct(productId, removeReason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sản phẩm đã được gỡ bỏ!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/{productId}/warn-agency")
    public ResponseEntity<Map<String, Object>> warnAgency(
            @PathVariable Long productId,
            @RequestBody Map<String, String> request) {
        try {
            String warningMessage = request.get("warningMessage");
            productManagementService.warnAgency(productId, warningMessage);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã gửi cảnh báo đến Agency!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    // Filter options endpoints
    @GetMapping("/filter-options")
    public ResponseEntity<Map<String, Object>> getFilterOptions() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("categories", productManagementService.getAllCategories());
            response.put("agencies", productManagementService.getAllAgencies());
            response.put("statuses", productManagementService.getAllProductStatuses());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error loading filter options: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
}