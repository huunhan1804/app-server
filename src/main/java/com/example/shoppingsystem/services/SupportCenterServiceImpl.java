package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.Message;
import com.example.shoppingsystem.entities.SupportArticle;
import com.example.shoppingsystem.entities.SupportCategory;
import com.example.shoppingsystem.repositories.SupportArticleRepository;
import com.example.shoppingsystem.repositories.SupportCategoryRepository;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.SupportCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SupportCenterServiceImpl implements SupportCenterService {
    private final SupportCategoryRepository supportCategoryRepository;
    private final SupportArticleRepository supportArticleRepository;

    @Autowired
    public SupportCenterServiceImpl(SupportCategoryRepository supportCategoryRepository, SupportArticleRepository supportArticleRepository) {
        this.supportCategoryRepository = supportCategoryRepository;
        this.supportArticleRepository = supportArticleRepository;
    }

    @Override
    public ApiResponse<List<SupportCategory>> getAllCategories(){
        return ApiResponse.<List<SupportCategory>>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.NOT_FOUND_CATEGORY)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<List<SupportArticle>> getArticlesByCategory(Long supportCategoryId){
        return ApiResponse.<List<SupportArticle>>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.NOT_FOUND_CATEGORY)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<SupportArticle> getArticleById(Long articleId){
        return ApiResponse.<SupportArticle>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.NOT_FOUND_ARTICLE)
                .timestamp(new java.util.Date())
                .build();
    }
}
