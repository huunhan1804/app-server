package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.ProductBasicDTO;
import com.example.shoppingsystem.dtos.ProductInfoDTO;
import com.example.shoppingsystem.filter.FilterService;
import com.example.shoppingsystem.requests.ListProductByParentCategoryRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.responses.SimplifiedResponse;
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

    @Operation(summary = "API info product by product-id", description = "This API get info of product by id.")
    @GetMapping("/info/{productId}")
    public ResponseEntity<ApiResponse<ProductInfoDTO>> getInfoProduct(@PathVariable long productId) {
        ApiResponse<ProductInfoDTO> apiResponse = productService.getInfoProduct(productId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

}
