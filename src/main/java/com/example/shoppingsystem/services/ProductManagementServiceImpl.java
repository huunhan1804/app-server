package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.StatusCode;
import com.example.shoppingsystem.dtos.ProductManagementDTO;
import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.ApprovalStatus;
import com.example.shoppingsystem.entities.Multimedia;
import com.example.shoppingsystem.entities.Product;
import com.example.shoppingsystem.repositories.*;
import com.example.shoppingsystem.responses.AgencyResponse;
import com.example.shoppingsystem.responses.CategoryResponse;
import com.example.shoppingsystem.services.interfaces.NotificationService;
import com.example.shoppingsystem.services.interfaces.ProductManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductManagementServiceImpl implements ProductManagementService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final ApprovalStatusRepository approvalStatusRepository;
    private final MultimediaRepository multimediaRepository;
    private final AgencyInfoRepository agencyInfoRepository;
    @Autowired
    private NotificationService notificationService;

    @Override
    public Page<ProductManagementDTO> getAllProducts(Pageable pageable, String status, String category, String agency, String keyword) {
        Specification<Product> spec = Specification.where(null);

        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("approvalStatus").get("statusCode"), status));
        }

        if (category != null && !category.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("category").get("categoryName"), category));
        }

        if (agency != null && !agency.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("account").get("fullname"), agency));
        }

        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")),
                            "%" + keyword.toLowerCase() + "%"));
        }

        Page<Product> products = productRepository.findAll(spec, pageable);

        List<ProductManagementDTO> productDTOs = products.getContent().stream()
                .map(this::convertToProductManagementDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(productDTOs, pageable, products.getTotalElements());
    }

    @Override
    public Page<ProductManagementDTO> getPendingProducts(Pageable pageable) {
        Page<Product> pendingProducts = productRepository.findByApprovalStatus_StatusCode(
                StatusCode.STATUS_PENDING, pageable);

        List<ProductManagementDTO> productDTOs = pendingProducts.getContent().stream()
                .map(this::convertToProductManagementDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(productDTOs, pageable, pendingProducts.getTotalElements());
    }

    @Override
    public ProductManagementDTO getProductForReview(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        return convertToProductManagementDTO(product);
    }

    @Override
    @Transactional
    public void approveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        ApprovalStatus approvedStatus = approvalStatusRepository.findApprovalStatusByStatusCode(StatusCode.STATUS_APPROVED);
        product.setApprovalStatus(approvedStatus);
        productRepository.save(product);

        notificationService.sendProductNotification(product.getAccount(), product.getProductName(),
                "Sản phẩm được phê duyệt",
                "Chúc mừng! Sản phẩm '" + product.getProductName() + "' đã được phê duyệt và có thể bán trên hệ thống.");

    }

    @Override
    @Transactional
    public void rejectProduct(Long productId, String rejectionReason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        ApprovalStatus rejectedStatus = approvalStatusRepository.findApprovalStatusByStatusCode(StatusCode.STATUS_REJECTED);
        product.setApprovalStatus(rejectedStatus);
        productRepository.save(product);

        notificationService.sendProductNotification(product.getAccount(), product.getProductName(),
                "Sản phẩm bị từ chối",
                "Sản phẩm '" + product.getProductName() + "' đã bị từ chối. Lý do: " + rejectionReason +
                        ". Vui lòng chỉnh sửa sản phẩm theo yêu cầu và gửi lại để được duyệt.");

    }

    @Override
    @Transactional
    public void requestProductEdit(Long productId, String editNotes) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Gửi thông báo yêu cầu chỉnh sửa đến Agency
        notificationService.sendProductNotification(product.getAccount(), product.getProductName(),
                "Yêu cầu chỉnh sửa sản phẩm",
                "Sản phẩm '" + product.getProductName() + "' cần được chỉnh sửa. Ghi chú từ admin: " + editNotes +
                        ". Vui lòng cập nhật sản phẩm theo yêu cầu.");

    }

    @Override
    @Transactional
    public void removeProduct(Long productId, String removeReason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Gửi thông báo trước khi xóa
        sendNotificationToAgency(product.getAccount(), "Sản phẩm bị gỡ bỏ",
                "Sản phẩm '" + product.getProductName() + "' đã bị gỡ bỏ. Lý do: " + removeReason);

        // Xóa sản phẩm
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public void warnAgency(Long productId, String warningMessage) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Gửi cảnh báo đến Agency
        notificationService.sendProductNotification(product.getAccount(), product.getProductName(),
                "Cảnh báo vi phạm",
                "Cảnh báo về sản phẩm '" + product.getProductName() + "': " + warningMessage +
                        ". Vui lòng tuân thủ các quy định của hệ thống để tránh bị khóa tài khoản.");

    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> new CategoryResponse(
                        category.getCategoryId(),
                        category.getCategoryName(),
                        null
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<AgencyResponse> getAllAgencies() {
        return accountRepository.findByRoleRoleId(3L).stream() // Role ID 3 = Agency
                .map(account -> new AgencyResponse(
                        account.getAccountId(),
                        account.getFullname(),
                        account.getEmail()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllProductStatuses() {
        return approvalStatusRepository.findAll().stream()
                .map(ApprovalStatus::getStatusCode)
                .collect(Collectors.toList());
    }

    private ProductManagementDTO convertToProductManagementDTO(Product product) {
        // Lấy hình ảnh đại diện
        String imageUrl = multimediaRepository.findByProduct_ProductId(product.getProductId())
                .map(Multimedia::getMultimediaUrl)
                .orElse("https://via.placeholder.com/150");

        return ProductManagementDTO.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .imageUrl(imageUrl)
                .agencyName(product.getAccount().getFullname())
                .agencyEmail(product.getAccount().getEmail())
                .categoryName(product.getCategory().getCategoryName())
                .listPrice(product.getListPrice())
                .salePrice(product.getSalePrice())
                .inventoryQuantity(product.getInventoryQuantity())
                .soldAmount(product.getSoldAmount())
                .status(product.getApprovalStatus().getStatusCode())
                .statusName(product.getApprovalStatus().getStatusName())
                .createdDate(product.getCreatedDate())
                .updatedDate(product.getUpdatedDate())
                .build();
    }

    private void sendNotificationToAgency(Account agency, String title, String message) {
        notificationService.sendNotificationToAgency(agency, title, message);
    }

}