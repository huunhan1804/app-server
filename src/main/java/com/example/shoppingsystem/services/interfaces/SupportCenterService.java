package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.entities.SupportArticle;
import com.example.shoppingsystem.entities.SupportCategory;
import com.example.shoppingsystem.responses.ApiResponse;

import java.util.List;

public interface SupportCenterService {
    ApiResponse<List<SupportCategory>> getAllCategories();
    ApiResponse<List<SupportArticle>> getArticlesByCategory(Long supportCategoryId);
    ApiResponse<SupportArticle> getArticleById(Long articleId);
}
