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
import com.example.shoppingsystem.requests.*;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.SupportCenterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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


    @Override
    public ApiResponse<SupportCategoryDTO> createCategory(CreateCategoryRequest request) {
        try {
            // Kiểm tra tên danh mục đã tồn tại
            if (supportCategoryRepository.existsBySupportCategoryName(request.getCategoryName())) {
                return ApiResponse.<SupportCategoryDTO>builder()
                        .status(ErrorCode.CONFLICT)
                        .message("Tên danh mục đã tồn tại")
                        .timestamp(new Date())
                        .build();
            }

            SupportCategory category = SupportCategory.builder()
                    .supportCategoryName(request.getCategoryName())
                    .supportCategoryDescription(request.getDescription())
                    .build();

            SupportCategory savedCategory = supportCategoryRepository.save(category);

            return ApiResponse.<SupportCategoryDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message("Tạo danh mục thành công")
                    .data(convertToSupportCategoriesDTO(savedCategory))
                    .timestamp(new Date())
                    .build();

        } catch (Exception e) {
            return ApiResponse.<SupportCategoryDTO>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message("Lỗi khi tạo danh mục: " + e.getMessage())
                    .timestamp(new Date())
                    .build();
        }
    }

    @Override
    public ApiResponse<String> deleteCategory(Long categoryId) {
        try {
            Optional<SupportCategory> category = supportCategoryRepository.findById(categoryId);
            if (!category.isPresent()) {
                return ApiResponse.<String>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message("Không tìm thấy danh mục")
                        .timestamp(new Date())
                        .build();
            }

            // Kiểm tra xem danh mục có bài viết không
            List<SupportArticle> articles = supportArticleRepository
                    .findBySupportCategory_SupportCategoryId(categoryId);

            if (!articles.isEmpty()) {
                supportArticleRepository.deleteAll(articles);
            }

            supportCategoryRepository.delete(category.get());

            return ApiResponse.<String>builder()
                    .status(ErrorCode.SUCCESS)
                    .message("Xóa danh mục thành công")
                    .timestamp(new Date())
                    .build();

        } catch (Exception e) {
            return ApiResponse.<String>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message("Lỗi khi xóa danh mục: " + e.getMessage())
                    .timestamp(new Date())
                    .build();
        }
    }

    // === BÀI VIẾT ===

    @Override
    public ApiResponse<SupportArticleDTO> createArticle(Long categoryId, CreateArticleRequest request) {
        try {
            Optional<SupportCategory> category = supportCategoryRepository.findById(categoryId);
            if (!category.isPresent()) {
                return ApiResponse.<SupportArticleDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message("Không tìm thấy danh mục")
                        .timestamp(new Date())
                        .build();
            }

            // Lấy số thứ tự tiếp theo
            int nextOrder = supportArticleRepository
                    .countBySupportCategory_SupportCategoryId(categoryId) + 1;

            SupportArticle article = SupportArticle.builder()
                    .articleTitle(request.getArticleTitle())
                    .articleContent(request.getArticleContent())
                    .isVisible(true)
                    .viewCount(0)
                    .sortOrder(nextOrder)
                    .supportCategory(category.get())
                    .build();

            SupportArticle savedArticle = supportArticleRepository.save(article);

            return ApiResponse.<SupportArticleDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message("Tạo bài viết thành công")
                    .data(convertToSupportArticleDTO(savedArticle))
                    .timestamp(new Date())
                    .build();

        } catch (Exception e) {
            return ApiResponse.<SupportArticleDTO>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message("Lỗi khi tạo bài viết: " + e.getMessage())
                    .timestamp(new Date())
                    .build();
        }
    }

    @Override
    public ApiResponse<SupportArticleDTO> updateArticle(Long articleId, UpdateArticleRequest request) {
        try {
            Optional<SupportArticle> articleOpt = supportArticleRepository.findById(articleId);
            if (!articleOpt.isPresent()) {
                return ApiResponse.<SupportArticleDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message("Không tìm thấy bài viết")
                        .timestamp(new Date())
                        .build();
            }

            SupportArticle article = articleOpt.get();
            article.setArticleTitle(request.getArticleTitle());
            article.setArticleContent(request.getArticleContent());
            // Cập nhật danh mục nếu có
            if (request.getCategoryId() != null) {
                Optional<SupportCategory> newCategory = supportCategoryRepository
                        .findById(request.getCategoryId());
                if (newCategory.isPresent()) {
                    article.setSupportCategory(newCategory.get());
                }
            }

            SupportArticle updatedArticle = supportArticleRepository.save(article);

            return ApiResponse.<SupportArticleDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message("Cập nhật bài viết thành công")
                    .data(convertToSupportArticleDTO(updatedArticle))
                    .timestamp(new Date())
                    .build();

        } catch (Exception e) {
            return ApiResponse.<SupportArticleDTO>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message("Lỗi khi cập nhật bài viết: " + e.getMessage())
                    .timestamp(new Date())
                    .build();
        }
    }

    @Override
    public ApiResponse<SupportArticleDTO> toggleArticleVisibility(Long articleId) {
        try {
            Optional<SupportArticle> articleOpt = supportArticleRepository.findById(articleId);
            if (!articleOpt.isPresent()) {
                return ApiResponse.<SupportArticleDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message("Không tìm thấy bài viết")
                        .timestamp(new Date())
                        .build();
            }

            SupportArticle article = articleOpt.get();
            article.setIsVisible(!article.getIsVisible());

            SupportArticle updatedArticle = supportArticleRepository.save(article);

            String message = article.getIsVisible() ?
                    "Hiển thị bài viết thành công" : "Ẩn bài viết thành công";

            return ApiResponse.<SupportArticleDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(message)
                    .data(convertToSupportArticleDTO(updatedArticle))
                    .timestamp(new Date())
                    .build();

        } catch (Exception e) {
            return ApiResponse.<SupportArticleDTO>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message("Lỗi khi thay đổi trạng thái bài viết: " + e.getMessage())
                    .timestamp(new Date())
                    .build();
        }
    }

    @Override
    public ApiResponse<List<SupportArticleDTO>> reorderArticles(ReorderArticlesRequest request) {
        try {
            List<SupportArticle> articles = new ArrayList<>();

            for (int i = 0; i < request.getArticleIds().size(); i++) {
                Long articleId = request.getArticleIds().get(i);
                Optional<SupportArticle> articleOpt = supportArticleRepository.findById(articleId);

                if (articleOpt.isPresent()) {
                    SupportArticle article = articleOpt.get();
                    article.setSortOrder(i + 1);
                    articles.add(article);
                }
            }

            List<SupportArticle> savedArticles = supportArticleRepository.saveAll(articles);
            List<SupportArticleDTO> articleDTOs = savedArticles.stream()
                    .map(this::convertToSupportArticleDTO)
                    .collect(Collectors.toList());

            return ApiResponse.<List<SupportArticleDTO>>builder()
                    .status(ErrorCode.SUCCESS)
                    .message("Sắp xếp lại thứ tự bài viết thành công")
                    .data(articleDTOs)
                    .timestamp(new Date())
                    .build();

        } catch (Exception e) {
            return ApiResponse.<List<SupportArticleDTO>>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message("Lỗi khi sắp xếp lại bài viết: " + e.getMessage())
                    .timestamp(new Date())
                    .build();
        }
    }

    // Trong SupportCenterServiceImpl.java
    @Override
    public ApiResponse<SupportCategoryDTO> updateCategory(Long categoryId, UpdateCategoryRequest request) {
        try {
            Optional<SupportCategory> categoryOpt = supportCategoryRepository.findById(categoryId);
            if (!categoryOpt.isPresent()) {
                return ApiResponse.<SupportCategoryDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message("Không tìm thấy danh mục")
                        .timestamp(new Date())
                        .build();
            }

            SupportCategory category = categoryOpt.get();

            // Kiểm tra tên danh mục đã tồn tại (trừ danh mục hiện tại)
            Optional<SupportCategory> existingCategory = supportCategoryRepository
                    .findBySupportCategoryNameAndSupportCategoryIdNot(
                            request.getCategoryName(), categoryId);

            if (existingCategory.isPresent()) {
                return ApiResponse.<SupportCategoryDTO>builder()
                        .status(ErrorCode.CONFLICT)
                        .message("Tên danh mục đã tồn tại")
                        .timestamp(new Date())
                        .build();
            }

            // Cập nhật thông tin
            category.setSupportCategoryName(request.getCategoryName());
            category.setSupportCategoryDescription(request.getDescription());

            SupportCategory updatedCategory = supportCategoryRepository.save(category);

            return ApiResponse.<SupportCategoryDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message("Cập nhật danh mục thành công")
                    .data(convertToSupportCategoriesDTO(updatedCategory))
                    .timestamp(new Date())
                    .build();

        } catch (Exception e) {
            return ApiResponse.<SupportCategoryDTO>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message("Lỗi khi cập nhật danh mục: " + e.getMessage())
                    .timestamp(new Date())
                    .build();
        }
    }

    @Override
    public ApiResponse<String> deleteArticle(Long articleId) {
        try {
            Optional<SupportArticle> articleOpt = supportArticleRepository.findById(articleId);
            if (!articleOpt.isPresent()) {
                return ApiResponse.<String>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.NOT_FOUND_ARTICLE)
                        .timestamp(new Date())
                        .build();
            }

            SupportArticle article = articleOpt.get();
            Long categoryId = article.getSupportCategory().getSupportCategoryId();
            String articleTitle = article.getArticleTitle();

            // Xóa bài viết
            supportArticleRepository.delete(article);

            // Cập nhật lại thứ tự của các bài viết còn lại trong cùng danh mục
            reorderRemainingArticles(categoryId);

            return ApiResponse.<String>builder()
                    .status(ErrorCode.SUCCESS)
                    .message("Xóa bài viết '" + articleTitle + "' thành công")
                    .timestamp(new Date())
                    .build();

        } catch (Exception e) {
            return ApiResponse.<String>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message("Lỗi khi xóa bài viết: " + e.getMessage())
                    .timestamp(new Date())
                    .build();
        }
    }

    // Helper method để sắp xếp lại thứ tự các bài viết sau khi xóa
    private void reorderRemainingArticles(Long categoryId) {
        List<SupportArticle> remainingArticles = supportArticleRepository
                .findBySupportCategory_SupportCategoryIdOrderBySortOrderAsc(categoryId);

        for (int i = 0; i < remainingArticles.size(); i++) {
            SupportArticle article = remainingArticles.get(i);
            article.setSortOrder(i + 1);
        }

        supportArticleRepository.saveAll(remainingArticles);
    }
}
