package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.SupportArticleDTO;
import com.example.shoppingsystem.dtos.SupportCategoryDTO;
import com.example.shoppingsystem.requests.*;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.SupportCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/support")
@CrossOrigin(origins = "*")
public class SupportLibraryController {

    @Autowired
    private SupportCenterService supportCenterService;

    // === QUẢN LÝ DANH MỤC ===

    @PostMapping("/categories")
    public ApiResponse<SupportCategoryDTO> createCategory(
            @RequestBody CreateCategoryRequest request) {
        return supportCenterService.createCategory(request);
    }

    @GetMapping("/categories")
    public ApiResponse<List<SupportCategoryDTO>> getAllCategories() {
        return supportCenterService.getAllCategories();
    }

    @PutMapping("/categories/{categoryId}")
    public ApiResponse<SupportCategoryDTO> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody UpdateCategoryRequest request) {
        return supportCenterService.updateCategory(categoryId, request);
    }

    @DeleteMapping("/categories/{categoryId}")
    public ApiResponse<String> deleteCategory(@PathVariable Long categoryId) {
        return supportCenterService.deleteCategory(categoryId);
    }

    // === QUẢN LÝ BÀI VIẾT ===

    @PostMapping("/categories/{categoryId}/articles")
    public ApiResponse<SupportArticleDTO> createArticle(
            @PathVariable Long categoryId,
            @RequestBody CreateArticleRequest request) {
        return supportCenterService.createArticle(categoryId, request);
    }

    @GetMapping("/categories/{categoryId}/articles")
    public ApiResponse<List<SupportArticleDTO>> getArticlesByCategory(
            @PathVariable Long categoryId) {
        return supportCenterService.getArticlesByCategory(categoryId);
    }

    @PutMapping("/articles/{articleId}")
    public ApiResponse<SupportArticleDTO> updateArticle(
            @PathVariable Long articleId,
            @RequestBody UpdateArticleRequest request) {
        return supportCenterService.updateArticle(articleId, request);
    }

    @DeleteMapping("/articles/{articleId}")
    public ApiResponse<String> deleteArticle(@PathVariable Long articleId) {
        return supportCenterService.deleteArticle(articleId);
    }

    @PostMapping("/articles/{articleId}/toggle-visibility")
    public ApiResponse<SupportArticleDTO> toggleArticleVisibility(
            @PathVariable Long articleId) {
        return supportCenterService.toggleArticleVisibility(articleId);
    }

    @PostMapping("/articles/reorder")
    public ApiResponse<List<SupportArticleDTO>> reorderArticles(
            @RequestBody ReorderArticlesRequest request) {
        return supportCenterService.reorderArticles(request);
    }
}