package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.Message;
import com.example.shoppingsystem.dtos.SupportArticleDTO;
import com.example.shoppingsystem.dtos.SupportCategoryDTO;
import com.example.shoppingsystem.entities.SupportArticle;
import com.example.shoppingsystem.entities.SupportCategory;
import com.example.shoppingsystem.repositories.AccountRepository;
import com.example.shoppingsystem.repositories.AgencyInfoRepository;
import com.example.shoppingsystem.repositories.SupportArticleRepository;
import com.example.shoppingsystem.repositories.SupportCategoryRepository;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.SupportCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SupportCenterServiceImpl implements SupportCenterService {
    private final SupportCategoryRepository supportCategoryRepository;
    private final SupportArticleRepository supportArticleRepository;

    @Autowired
    public SupportCenterServiceImpl(SupportCategoryRepository supportCategoryRepository, SupportArticleRepository supportArticleRepository, AccountRepository accountRepository, AgencyInfoRepository agencyInfoRepository) {
        this.supportCategoryRepository = supportCategoryRepository;
        this.supportArticleRepository = supportArticleRepository;
    }

    @Override
    public ApiResponse<List<SupportCategoryDTO>> getAllCategories(){
        List<SupportCategory> categories = supportCategoryRepository.findAll();
        if (!categories.isEmpty()) {
            List<SupportCategoryDTO> categoriesDTOs = new ArrayList<>();
            for (SupportCategory category : categories) {
                SupportCategoryDTO categoryDTO = convertToSupportCategoriesDTO(category);
                categoriesDTOs.add(categoryDTO);
            }
            return ApiResponse.<List<SupportCategoryDTO>>builder()
                    .status(ErrorCode.SUCCESS)
                    .data(categoriesDTOs)
                    .message(Message.FETCHING_SUPPORT_CATEGORY_SUCCESS)
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<List<SupportCategoryDTO>>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.FETCHING_SUPPORT_CATEGORY_FAILURE)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<List<SupportArticleDTO>> getArticlesByCategory(Long supportCategoryId){
        Optional<SupportCategory> category = supportCategoryRepository.findById(supportCategoryId);
        if(category.isPresent()) {
            List<SupportArticle> supportArticles = supportArticleRepository.findBySupportCategory_SupportCategoryId(supportCategoryId);
            if(!supportArticles.isEmpty()) {
                List<SupportArticleDTO> articleDTOs = new ArrayList<>();
                for (SupportArticle supportArticle : supportArticles) {
                    SupportArticleDTO supportArticleDTO = convertToSupportArticleDTO(supportArticle);
                    articleDTOs.add(supportArticleDTO);
                }
                return ApiResponse.<List<SupportArticleDTO>>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.FETCHING_SUPPORT_ARTICLE_SUCCESS)
                        .data(articleDTOs)
                        .timestamp(new java.util.Date())
                        .build();
            }
            return ApiResponse.<List<SupportArticleDTO>>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.NOT_FOUND_ARTICLE)
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<List<SupportArticleDTO>>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.NOT_FOUND_CATEGORY)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<SupportArticleDTO> getArticleById(Long articleId){
        Optional<SupportArticle> article = supportArticleRepository.findById(articleId);
        if(article.isPresent()) {
            SupportArticle supportArticle = article.get();
            SupportArticleDTO supportArticleDTO = convertToSupportArticleDTO(supportArticle);
            supportArticle.setViewCount(supportArticle.getViewCount() + 1);
            supportArticleRepository.save(supportArticle);
            return ApiResponse.<SupportArticleDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.FETCHING_SUPPORT_ARTICLE_SUCCESS)
                    .data(supportArticleDTO)
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<SupportArticleDTO>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.NOT_FOUND_ARTICLE)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<List<SupportArticleDTO>> hideArticles(Long supportArticleId){
        Optional<SupportArticle> article = supportArticleRepository.findById(supportArticleId);
        if (article.isPresent()) {
            SupportArticle supportArticle = article.get();
            if(supportArticle.getIsVisible()){
                supportArticle.setIsVisible(false);
                supportArticleRepository.save(supportArticle);
                return ApiResponse.<List<SupportArticleDTO>>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.HIDE_ARTICLE_SUCCESS)
                        .data(getAllArticlesByCategory(supportArticle.getSupportCategory().getSupportCategoryId()))
                        .timestamp(new java.util.Date())
                        .build();
            }
            return ApiResponse.<List<SupportArticleDTO>>builder()
                    .status(ErrorCode.CONFLICT)
                    .message(Message.HIDE_ARTICLE_FAILURE)
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<List<SupportArticleDTO>>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.NOT_FOUND_ARTICLE)
                .timestamp(new java.util.Date())
                .build();
    }

    public SupportArticleDTO convertToSupportArticleDTO(SupportArticle supportArticle){
        SupportArticleDTO supportArticleDTO = new SupportArticleDTO();
        supportArticleDTO.setCategory(convertToSupportCategoriesDTO(supportArticle.getSupportCategory()));
        supportArticleDTO.setArticleId(supportArticle.getArticleId());
        supportArticleDTO.setArticleTitle(supportArticle.getArticleTitle());
        supportArticleDTO.setArticleContent(supportArticle.getArticleContent());
        supportArticleDTO.setIsVisible(supportArticle.getIsVisible());
        supportArticleDTO.setViewCount(supportArticle.getViewCount());
        return supportArticleDTO;
    }

    public SupportCategoryDTO convertToSupportCategoriesDTO(SupportCategory supportCategory){
        SupportCategoryDTO supportCategoryDTO = new SupportCategoryDTO();
        supportCategoryDTO.setSupportCategoryId(supportCategory.getSupportCategoryId());
        supportCategoryDTO.setSupportCategoryName(supportCategory.getSupportCategoryName());
        supportCategoryDTO.setSupportCategoryDescription(supportCategory.getSupportCategoryDescription());
        return supportCategoryDTO;
    }

    public List<SupportArticleDTO> getAllArticlesByCategory(Long supportCategoryId){
        List<SupportArticle> supportArticles = supportArticleRepository.findBySupportCategory_SupportCategoryId(supportCategoryId);
        List<SupportArticleDTO> supportArticleDTOs = new ArrayList<>();
        for (SupportArticle supportArticle : supportArticles) {
            SupportArticleDTO supportArticleDTO = convertToSupportArticleDTO(supportArticle);
            supportArticleDTOs.add(supportArticleDTO);
        }
        return supportArticleDTOs;
    }
}
