package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.ProductBasicDTO;
import com.example.shoppingsystem.dtos.ProductInfoDTO;
import com.example.shoppingsystem.entities.Product;
import com.example.shoppingsystem.entities.ProductVariant;
import com.example.shoppingsystem.requests.ListProductByParentCategoryRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.responses.SimplifiedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ProductService {
    ApiResponse<List> getListProductByParentCategory(long listProductByParentCategoryRequest);
    ApiResponse<ProductInfoDTO> getInfoProduct(long productId);
    Optional<Product> getProductByID(long productId);
    Optional<ProductVariant> getProductVariantByID(long productVariantId);
    ApiResponse<List> getListBestSellerProduct();

    ApiResponse<List> getListBestOrderProduct();

    ApiResponse<List<ProductBasicDTO>> getListRelatedProduct(long productId);

    ApiResponse<List<ProductBasicDTO>> searchProductsByKeyword(String keyword);

    ApiResponse<List> getListProductByCategory(Long categoryId);
}
