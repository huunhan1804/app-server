package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.ProductManagementDTO;
import com.example.shoppingsystem.responses.CategoryResponse;
import com.example.shoppingsystem.responses.AgencyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductManagementService {
    Page<ProductManagementDTO> getAllProducts(Pageable pageable, String status, String category, String agency, String keyword);
    Page<ProductManagementDTO> getPendingProducts(Pageable pageable);
    ProductManagementDTO getProductForReview(Long productId);
    void approveProduct(Long productId);
    void rejectProduct(Long productId, String rejectionReason);
    void requestProductEdit(Long productId, String editNotes);
    void removeProduct(Long productId, String removeReason);
    void warnAgency(Long productId, String warningMessage);
    List<CategoryResponse> getAllCategories();
    List<AgencyResponse> getAllAgencies();
    List<String> getAllProductStatuses();
}