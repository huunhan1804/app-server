package com.example.shoppingsystem.api;

import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.responses.CategoryResponse;
import com.example.shoppingsystem.services.interfaces.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Tag(name = "Category")
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "API all category by parent-category", description = "This API get list category by one parent-category.")
    @GetMapping("/all/{parentCategoryId}")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllByParentCategory(@PathVariable Long parentCategoryId) {
        ApiResponse<List<CategoryResponse>> apiResponse = categoryService.getListCategory(parentCategoryId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }
}
