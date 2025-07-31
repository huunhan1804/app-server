package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.StatusCode;
import com.example.shoppingsystem.dtos.ProductManagementDTO;
import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.ApprovalStatus;
import com.example.shoppingsystem.entities.Multimedia;
import com.example.shoppingsystem.entities.Product;
import com.example.shoppingsystem.enums.MultimediaType;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
    public Page<ProductManagementDTO> getAllProducts(Pageable pageable, String status,
                                                     Long categoryId, Long agencyId, String keyword) {
        try {
            System.out.println("Service filtering with: status=" + status +
                    ", categoryId=" + categoryId + ", agencyId=" + agencyId + ", keyword=" + keyword);

            Page<Product> productPage = productRepository.findProductsWithFilters(
                    status, categoryId, agencyId, keyword, pageable);

            System.out.println("Found " + productPage.getTotalElements() + " products");

            return productPage.map(this::convertToDTO);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private ProductManagementDTO convertToDTO(Product product) {
        return ProductManagementDTO.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .imageUrl(getProductImageUrl(product)) // Helper method to get image
                .agencyName(product.getAgencyInfo().getAccount().getFullname())
                .agencyEmail(product.getAgencyInfo().getAccount().getEmail())
                .categoryName(product.getCategory().getCategoryName())
                .listPrice(product.getListPrice())
                .salePrice(product.getSalePrice())
                .inventoryQuantity(product.getInventoryQuantity())
                .soldAmount(product.getSoldAmount() != null ? product.getSoldAmount() : 0)
                .status(product.getApprovalStatus().getStatusCode())
                .statusName(product.getApprovalStatus().getStatusName())
                .createdDate(product.getCreatedDate())
                .updatedDate(product.getUpdatedDate())
                .build();
    }

    private String getProductImageUrl(Product product) {
        try {
            // Lấy hình ảnh theo loại
            List<Multimedia> multimediaList = multimediaRepository.findAllByProduct_ProductId(product.getProductId());

            // Lọc chỉ lấy hình ảnh (không phải video)
            Optional<Multimedia> imageMultimedia = multimediaList.stream()
                    .filter(m -> m.getMultimediaType() == MultimediaType.IMAGE)
                    .findFirst();

            if (imageMultimedia.isPresent()) {
                return imageMultimedia.get().getMultimediaUrl();
            }

            return getDefaultProductImage();

        } catch (Exception e) {
            System.err.println("Error getting product image for product ID: " + product.getProductId());
            e.printStackTrace();
            return getDefaultProductImage();
        }
    }

    // ← Thêm method cho hình ảnh mặc định
    private String getDefaultProductImage() {
        return "https://via.placeholder.com/300x300?text=No+Image";
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

        notificationService.sendProductNotification(product.getAgencyInfo().getAccount(), product.getProductName(),
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

        notificationService.sendProductNotification(product.getAgencyInfo().getAccount(), product.getProductName(),
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
        notificationService.sendProductNotification(product.getAgencyInfo().getAccount(), product.getProductName(),
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
        sendNotificationToAgency(product.getAgencyInfo().getAccount(), "Sản phẩm bị gỡ bỏ",
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
        notificationService.sendProductNotification(product.getAgencyInfo().getAccount(), product.getProductName(),
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
        String imageUrl = multimediaRepository.findAllByProduct_ProductId(product.getProductId())
                .stream()
                .findFirst()
                .map(Multimedia::getMultimediaUrl)
                .orElse("https://via.placeholder.com/150");

        return ProductManagementDTO.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .imageUrl(imageUrl)
                .agencyName(product.getAgencyInfo().getAccount().getFullname())
                .agencyEmail(product.getAgencyInfo().getAccount().getEmail())
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