package com.example.shoppingsystem.services;

import com.example.shoppingsystem.entities.Product;
import com.example.shoppingsystem.entities.Category;
import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.ApprovalStatus;
import com.example.shoppingsystem.repositories.ProductRepository;
import com.example.shoppingsystem.repositories.CategoryRepository;
import com.example.shoppingsystem.repositories.AccountRepository;
import com.example.shoppingsystem.repositories.MultimediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProductManagementService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MultimediaRepository multimediaRepository;

    public Map<String, Object> getAllProducts(Pageable pageable, String search, String status,
                                              Long categoryId, Long agencyId) {
        Specification<Product> spec = createProductSpecification(search, status, categoryId, agencyId);
        Page<Product> productsPage = productRepository.findAll(spec, pageable);

        List<Map<String, Object>> productList = new ArrayList<>();

        for (Product product : productsPage.getContent()) {
            Map<String, Object> productData = new HashMap<>();
            productData.put("productId", product.getProductId());
            productData.put("productName", product.getProductName());
            productData.put("shortDescription", product.getShortDescription());
            productData.put("listPrice", product.getListPrice());
            productData.put("salePrice", product.getSalePrice());
            productData.put("inventoryQuantity", product.getInventoryQuantity());
            productData.put("soldAmount", product.getSoldAmount());
            productData.put("productStatus", product.getProductStatus());
            productData.put("approvalStatus", product.getApprovalStatus());
            productData.put("averageRating", product.getAverageRating());
            productData.put("totalReviews", product.getTotalReviews());
            productData.put("createdDate", product.getCreatedDate());

            if (product.getCategory() != null) {
                productData.put("categoryName", product.getCategory().getCategoryName());
                productData.put("categoryId", product.getCategory().getCategoryId());
            }

            if (product.getAccount() != null) {
                productData.put("agencyName", product.getAccount().getFullname());
                productData.put("agencyId", product.getAccount().getAccountId());
            }

            String primaryImage = multimediaRepository.findPrimaryImageByProductId(product.getProductId());
            productData.put("primaryImage", primaryImage);

            productList.add(productData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", Map.of(
                "content", productList,
                "totalElements", productsPage.getTotalElements(),
                "totalPages", productsPage.getTotalPages(),
                "number", productsPage.getNumber(),
                "size", productsPage.getSize()
        ));

        return result;
    }

    public Map<String, Object> getPendingProducts() {
        // 🔁 Sửa lại cho đúng nếu không có enum ApprovalStatusCode
        List<Product> pendingProducts = productRepository.findByApprovalStatus_StatusCode("PROCESSING");

        List<Map<String, Object>> productList = new ArrayList<>();
        for (Product product : pendingProducts) {
            Map<String, Object> productData = createProductMap(product);
            productList.add(productData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("pendingProducts", productList);
        result.put("count", pendingProducts.size());

        return result;
    }

    public Map<String, Object> approveProduct(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);

        if (!productOpt.isPresent()) {
            throw new RuntimeException("Product not found");
        }

        Product product = productOpt.get();

        // Gán trạng thái duyệt APPROVED – bạn phải lấy từ DB hoặc Enum String (ví dụ)
        ApprovalStatus approvedStatus = new ApprovalStatus();
        approvedStatus.setStatusCode("APPROVED");
        product.setApprovalStatus(approvedStatus);

        // Gán trạng thái hoạt động nếu có enum ProductStatus
        product.setProductStatus("ACTIVE"); // String hoặc enum tuỳ thiết kế
        product.setUpdatedDate(LocalDateTime.now());

        productRepository.save(product);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Product approved successfully");
        result.put("product", createProductMap(product));

        return result;
    }

    public Map<String, Object> rejectProduct(Long productId, String reason) {
        Optional<Product> productOpt = productRepository.findById(productId);

        if (!productOpt.isPresent()) {
            throw new RuntimeException("Product not found");
        }

        Product product = productOpt.get();

        ApprovalStatus declinedStatus = new ApprovalStatus();
        declinedStatus.setStatusCode("DECLINED");
        product.setApprovalStatus(declinedStatus);

        product.setRejectionReason(reason);
        product.setUpdatedDate(LocalDateTime.now());

        productRepository.save(product);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Product rejected successfully");
        result.put("product", createProductMap(product));

        return result;
    }

    public Map<String, Object> getProductDetail(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);

        if (!productOpt.isPresent()) {
            throw new RuntimeException("Product not found");
        }

        Product product = productOpt.get();
        Map<String, Object> productDetail = createDetailedProductMap(product);

        Map<String, Object> result = new HashMap<>();
        result.put("product", productDetail);

        return result;
    }

    public Map<String, Object> deleteProduct(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);

        if (!productOpt.isPresent()) {
            throw new RuntimeException("Product not found");
        }

        Product product = productOpt.get();

        product.setProductStatus("INACTIVE");
        product.setUpdatedDate(LocalDateTime.now());

        productRepository.save(product);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Product deleted successfully");

        return result;
    }

    private Specification<Product> createProductSpecification(String search, String status,
                                                              Long categoryId, Long agencyId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("productName")),
                        "%" + search.toLowerCase() + "%"
                ));
            }

            if (status != null && !status.isEmpty()) {
                if (status.equalsIgnoreCase("active")) {
                    predicates.add(criteriaBuilder.equal(root.get("productStatus"), "ACTIVE"));
                } else if (status.equalsIgnoreCase("pending")) {
                    predicates.add(criteriaBuilder.equal(root.get("approvalStatus").get("statusCode"), "PROCESSING"));
                } else if (status.equalsIgnoreCase("rejected")) {
                    predicates.add(criteriaBuilder.equal(root.get("approvalStatus").get("statusCode"), "DECLINED"));
                }
            }

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("categoryId"), categoryId));
            }

            if (agencyId != null) {
                predicates.add(criteriaBuilder.equal(root.get("account").get("accountId"), agencyId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Map<String, Object> createProductMap(Product product) {
        Map<String, Object> productData = new HashMap<>();
        productData.put("productId", product.getProductId());
        productData.put("productName", product.getProductName());
        productData.put("shortDescription", product.getShortDescription());
        productData.put("listPrice", product.getListPrice());
        productData.put("salePrice", product.getSalePrice());
        productData.put("inventoryQuantity", product.getInventoryQuantity());
        productData.put("soldAmount", product.getSoldAmount());
        productData.put("productStatus", product.getProductStatus());
        productData.put("approvalStatus", product.getApprovalStatus());
        productData.put("createdDate", product.getCreatedDate());

        if (product.getCategory() != null) {
            productData.put("categoryName", product.getCategory().getCategoryName());
        }

        if (product.getAccount() != null) {
            productData.put("agencyName", product.getAccount().getFullname());
        }

        return productData;
    }

    private Map<String, Object> createDetailedProductMap(Product product) {
        Map<String, Object> productData = createProductMap(product);

        productData.put("productDescription", product.getProductDescription());
        productData.put("ingredients", product.getIngredients());
        productData.put("usageInstructions", product.getUsageInstructions());
        productData.put("contraindications", product.getContraindications());
        productData.put("origin", product.getOrigin());
        productData.put("manufacturer", product.getManufacturer());
        productData.put("expiryMonths", product.getExpiryMonths());
        productData.put("averageRating", product.getAverageRating());
        productData.put("totalReviews", product.getTotalReviews());
        productData.put("rejectionReason", product.getRejectionReason());

        List<Map<String, Object>> images = multimediaRepository.findImagesByProductId(product.getProductId());
        productData.put("images", images);

        return productData;
    }
}
