package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.Message;
import com.example.shoppingsystem.dtos.AccountInfoDTO;
import com.example.shoppingsystem.entities.Category;
import com.example.shoppingsystem.entities.Multimedia;
import com.example.shoppingsystem.repositories.CategoryRepository;
import com.example.shoppingsystem.repositories.MultimediaRepository;
import com.example.shoppingsystem.repositories.ParentCategoryRepository;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.responses.CategoryResponse;
import com.example.shoppingsystem.services.interfaces.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class CategoryServiceImpl implements CategoryService {
    private final ParentCategoryRepository parentCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final MultimediaRepository multimediaRepository;

    @Autowired
    public CategoryServiceImpl(ParentCategoryRepository parentCategoryRepository, CategoryRepository categoryRepository, MultimediaRepository multimediaRepository) {
        this.parentCategoryRepository = parentCategoryRepository;
        this.categoryRepository = categoryRepository;
        this.multimediaRepository = multimediaRepository;
    }


    @Override
    public ApiResponse<List<CategoryResponse>> getListCategory(long parentCategoryId) {
        List<Category> categoryList = categoryRepository.findAllByParentCategory_ParentCategoryId(parentCategoryId);
        List<CategoryResponse> categoryResponses = new ArrayList<>();
        categoryResponses.add(new CategoryResponse(0L,"All", "https://dl.dropbox.com/scl/fi/esvpgf0fxgsijlhls9uli/image-5.png?rlkey=6civ8b4oi9175l9p7g6ljh5bh&dl=0"));
        for (Category category : categoryList){
            Optional<Multimedia> multimedia = multimediaRepository.findByCategory_CategoryId(category.getCategoryId());
            if(multimedia.isPresent()){
                categoryResponses.add(new CategoryResponse(category.getCategoryId() ,category.getCategoryName(), multimedia.get().getMultimediaUrl()));
            }
        }

        return ApiResponse.<List<CategoryResponse>>builder()
                .status(ErrorCode.SUCCESS)
                .message(Message.SUCCESS)
                .data(categoryResponses)
                .timestamp(new Date())
                .build();
    }
}
