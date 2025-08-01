package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.OrderDTO;
import com.example.shoppingsystem.dtos.SupportArticleDTO;
import com.example.shoppingsystem.dtos.SupportCategoryDTO;
import com.example.shoppingsystem.requests.ListOrderByStatusRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.SupportCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@Tag(name = "Support Center")
public class SupportCenterController {
    private final SupportCenterService supportCenterService;

    @Operation(summary = "Get all support categories", description = "Get all support categories")
    @PostMapping("/all-categories/")
    public ResponseEntity<ApiResponse<List<SupportCategoryDTO>>> getAllCategories(){
        ApiResponse<List<SupportCategoryDTO>> response = supportCenterService.getAllCategories();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Get all support categories", description = "Get all support categories")
    @PostMapping("/all-by-category/{category_id}")
    public ResponseEntity<ApiResponse<List<SupportArticleDTO>>> getArticlesByCategory(
            @PathVariable Long category_id
    ){
        ApiResponse<List<SupportArticleDTO>> response = supportCenterService.getArticlesByCategory(category_id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Get support article", description = "Get support article")
    @PostMapping("/article/{article_id}")
    public ResponseEntity<ApiResponse<SupportArticleDTO>> getArticleById(
            @PathVariable Long article_id
    ){
        ApiResponse<SupportArticleDTO> response = supportCenterService.getArticleById(article_id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @Operation(summary = "Hide support article", description = "Hide support article")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/hide/{article_id}")
    public ResponseEntity<ApiResponse<List<SupportArticleDTO>>> hideArticles(
            @PathVariable Long article_id
    ){
        ApiResponse<List<SupportArticleDTO>> response = supportCenterService.hideArticles(article_id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}
