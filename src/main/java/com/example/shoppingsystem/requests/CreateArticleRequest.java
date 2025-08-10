package com.example.shoppingsystem.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateArticleRequest {
    @NotBlank(message = "Tiêu đề bài viết không được để trống")
    private String articleTitle;

    @NotBlank(message = "Nội dung bài viết không được để trống")
    private String articleContent;
}

