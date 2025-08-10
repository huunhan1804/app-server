package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.SupportArticleDTO;
import com.example.shoppingsystem.dtos.SupportCategoryDTO;
import com.example.shoppingsystem.entities.SupportArticle;
import com.example.shoppingsystem.entities.SupportCategory;
import com.example.shoppingsystem.requests.*;
import com.example.shoppingsystem.responses.ApiResponse;

import java.util.List;

public interface SupportCenterService {
    ApiResponse<List<SupportCategoryDTO>> getAllCategories();
    ApiResponse<List<SupportArticleDTO>> getArticlesByCategory(Long supportCategoryId);
    ApiResponse<SupportArticleDTO> getArticleById(Long articleId);
    ApiResponse<List<SupportArticleDTO>> hideArticles(Long supportArticleId);
    ApiResponse<SupportCategoryDTO> createCategory(CreateCategoryRequest request);
    ApiResponse<String> deleteCategory(Long categoryId);
    ApiResponse<String> deleteArticle(Long articleId);
    ApiResponse<SupportArticleDTO> createArticle(Long categoryId, CreateArticleRequest request);
    ApiResponse<SupportArticleDTO> updateArticle(Long articleId, UpdateArticleRequest request);
    ApiResponse<SupportArticleDTO> toggleArticleVisibility(Long articleId);
    ApiResponse<List<SupportArticleDTO>> reorderArticles(ReorderArticlesRequest request);
    ApiResponse<SupportCategoryDTO> updateCategory(Long categoryId, UpdateCategoryRequest request);
}
