package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.responses.CategoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    ApiResponse<List<CategoryResponse>> getListCategory(long parentCategoryId);
}
