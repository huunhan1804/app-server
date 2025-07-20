package com.example.shoppingsystem.controllers.web;

import com.example.shoppingsystem.repositories.*;
import com.example.shoppingsystem.utils.SampleDataGenerator;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/data")
@RequiredArgsConstructor

public class SampleDataController {
    private final EntityManager entityManager;
    private final SampleDataGenerator sampleDataGenerator;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final ApprovalStatusRepository approvalStatusRepository;
    private final ParentCategoryRepository parentCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final MultimediaRepository multimediaRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CouponRepository couponRepository;
    private final AccountCouponRepository accountCouponRepository;
    private final CouponOrderListRepository couponOrderListRepository;
    private final FeedbackRepository feedbackRepository;
    private final AddressRepository addressRepository;
    private final AgencyInfoRepository agencyInfoRepository;
    private final MembershipRepository membershipRepository;

    @GetMapping("/generate")
    public ResponseEntity<String> generateSampleData() {
        try {
            sampleDataGenerator.generateSampleData();
            return ResponseEntity.ok("Đã tạo thành công dữ liệu mẫu cho tất cả các bảng!");
        } catch (Exception e) {
            // Log chi tiết lỗi
            e.printStackTrace();

            String errorDetails = "Lỗi khi tạo dữ liệu: " + e.getMessage();

            if (e.getCause() != null) {
                errorDetails += "\nCause: " + e.getCause().getMessage();
            }

            // Lấy stack trace
            StringBuilder stackTrace = new StringBuilder();
            for (StackTraceElement element : e.getStackTrace()) {
                stackTrace.append(element.toString()).append("\n");
            }

            return ResponseEntity.internalServerError()
                    .body(errorDetails + "\n\nStack Trace:\n" + stackTrace.toString());
        }
    }

    @GetMapping("/generate-big")
    public ResponseEntity<String> generateBigSampleData() {
        try {
            sampleDataGenerator.generateBigSampleData();
            return ResponseEntity.ok("Đã tạo thành công dữ liệu mẫu lớn (10K+ records)!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Lỗi khi tạo dữ liệu lớn: " + e.getMessage());
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getDataCount() {
        Map<String, Long> counts = new HashMap<>();

        try {
            counts.put("roles", roleRepository.count());
            counts.put("approvalStatuses", approvalStatusRepository.count());
            counts.put("parentCategories", parentCategoryRepository.count());
            counts.put("categories", categoryRepository.count());
            counts.put("accounts", accountRepository.count());
            counts.put("memberships", membershipRepository.count());
            counts.put("agencyInfos", agencyInfoRepository.count());
            counts.put("addresses", addressRepository.count());
            counts.put("products", productRepository.count());
            counts.put("productVariants", productVariantRepository.count());
            counts.put("multimedia", multimediaRepository.count());
            counts.put("carts", cartRepository.count());
            counts.put("cartItems", cartItemRepository.count());
            counts.put("coupons", couponRepository.count());
            counts.put("accountCoupons", accountCouponRepository.count());
            counts.put("orders", orderRepository.count());
            counts.put("orderDetails", orderDetailRepository.count());
            counts.put("couponOrderLists", couponOrderListRepository.count());
            counts.put("feedbacks", feedbackRepository.count());

            // Tính tổng
            long total = counts.values().stream().mapToLong(Long::longValue).sum();
            counts.put("TOTAL", total);

            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            counts.put("ERROR", -1L);
            return ResponseEntity.internalServerError().body(counts);
        }
    }

    @GetMapping("/sample-data")
    public ResponseEntity<Map<String, Object>> getSampleData() {
        Map<String, Object> sampleData = new HashMap<>();

        try {
            // Lấy 5 records đầu tiên từ mỗi bảng
            sampleData.put("roles", roleRepository.findAll().stream().limit(5).toList());
            sampleData.put("accounts", accountRepository.findAll().stream().limit(5).toList());
            sampleData.put("products", productRepository.findAll().stream().limit(5).toList());
            sampleData.put("categories", categoryRepository.findAll().stream().limit(5).toList());
            sampleData.put("orders", orderRepository.findAll().stream().limit(5).toList());

            return ResponseEntity.ok(sampleData);
        } catch (Exception e) {
            sampleData.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(sampleData);
        }
    }

    @PostMapping("/clear-all")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<String> clearAllData() {
        try {
            System.out.println("🗑️ Bắt đầu xóa toàn bộ dữ liệu...");

            // Sử dụng deleteAllInBatch() để hiệu suất tốt hơn
            feedbackRepository.deleteAllInBatch();
            couponOrderListRepository.deleteAllInBatch();
            orderDetailRepository.deleteAllInBatch();
            orderRepository.deleteAllInBatch();
            accountCouponRepository.deleteAllInBatch();
            couponRepository.deleteAllInBatch();
            cartItemRepository.deleteAllInBatch();
            cartRepository.deleteAllInBatch();
            multimediaRepository.deleteAllInBatch();
            productVariantRepository.deleteAllInBatch();
            productRepository.deleteAllInBatch();
            addressRepository.deleteAllInBatch();
            agencyInfoRepository.deleteAllInBatch();
            membershipRepository.deleteAllInBatch();

            // Xóa accounts (trừ admin hiện tại)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = auth.getName();
            accountRepository.deleteByUsernameNot(currentUsername); // Giữ lại admin đang đăng nhập

            categoryRepository.deleteAllInBatch();
            parentCategoryRepository.deleteAllInBatch();
            approvalStatusRepository.deleteAllInBatch();

            // Không xóa roles để tránh lỗi hệ thống
            // roleRepository.deleteAllInBatch();

            System.out.println("✅ Đã xóa tất cả dữ liệu thành công!");
            return ResponseEntity.ok("Đã xóa tất cả dữ liệu thành công! (Giữ lại account admin hiện tại và roles)");

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xóa dữ liệu: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Lỗi khi xóa dữ liệu: " + e.getMessage());
        }
    }

    @GetMapping("/force-clear")
    @Transactional
    public ResponseEntity<String> forceClearData() {
        try {
            System.out.println("🗑️  Force clear all data...");

            // Tắt foreign key checks
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

            // Xóa tất cả bằng TRUNCATE (bỏ qua foreign key)
            String[] tables = {
                    "feedback", "coupon_order_list", "order_detail", "order_list",
                    "account_coupon", "coupon", "cart_item", "cart", "multimedia",
                    "product_variant", "product", "address", "seller_registration_application",
                    "membership", "account", "category", "parent_category",
                    "approval_status", "role"
            };

            for (String table : tables) {
                try {
                    entityManager.createNativeQuery("TRUNCATE TABLE " + table).executeUpdate();
                    System.out.println("✓ Truncated: " + table);
                } catch (Exception e) {
                    System.out.println("⚠️ Error truncating " + table + ": " + e.getMessage());
                }
            }

            // Bật lại foreign key checks
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

            System.out.println("✓ Force clear completed!");

            return ResponseEntity.ok("Đã xóa tất cả dữ liệu thành công bằng TRUNCATE!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Lỗi khi force clear: " + e.getMessage());
        }
    }
}