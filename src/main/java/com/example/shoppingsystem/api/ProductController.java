package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.ProductBasicDTO;
import com.example.shoppingsystem.dtos.ProductInfoDTO;
import com.example.shoppingsystem.filter.FilterService;
import com.example.shoppingsystem.requests.SearchRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Tag(name = "Product")
public class ProductController {
    private final ProductService productService;
    private final FilterService filterService;

    @Operation(summary = "API all product by parent-category", description = "This API get list product by one parent-category.")
    @GetMapping("/all-by-parent-category/{parentCategoryId}")
    public ResponseEntity<ApiResponse<List>> getAllByParentCategory(@PathVariable Long parentCategoryId) {
        ApiResponse<List> apiResponse = productService.getListProductByParentCategory(parentCategoryId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API all product by category", description = "This API get list product by category.")
    @GetMapping("/all-by-category/{categoryId}")
    public ResponseEntity<ApiResponse<List>> getAllByCategory(@PathVariable Long categoryId) {
        ApiResponse<List> apiResponse = productService.getListProductByCategory(categoryId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API info product by product-id", description = "This API get info of product by id.")
    @GetMapping("/info/{productId}")
    public ResponseEntity<ApiResponse<ProductInfoDTO>> getInfoProduct(@PathVariable long productId) {
        ApiResponse<ProductInfoDTO> apiResponse = productService.getInfoProduct(productId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API info product by product-id", description = "This API get info of product by id.")
    @GetMapping("/best-seller")
    public ResponseEntity<ApiResponse<List>> getListBestSellerProduct() {
        ApiResponse<List> apiResponse = productService.getListBestSellerProduct();
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API info product by product-id", description = "This API get info of product by id.")
    @GetMapping("/best-order")
    public ResponseEntity<ApiResponse<List>> getListBestOrderProduct() {
        ApiResponse<List> apiResponse = productService.getListBestOrderProduct();
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API info product by product-id", description = "This API get info of product by id.")
    @GetMapping("/related/{productId}")
    public ResponseEntity<ApiResponse<List<ProductBasicDTO>>> getListRelatedProduct(@PathVariable long productId) {
        ApiResponse<List<ProductBasicDTO>> apiResponse = productService.getListRelatedProduct(productId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API get list product by keyword", description = "This API get list product by keyword.")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductBasicDTO>>> getListSearchProduct(@RequestBody SearchRequest searchRequest) {
        ApiResponse<List<ProductBasicDTO>> apiResponse = productService.searchProductsByKeyword(searchRequest.getKeyword());
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }



}
