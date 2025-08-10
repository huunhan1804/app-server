package com.example.shoppingsystem.requests;

import lombok.Data;

@Data
public class UpdateArticleRequest {
    private String articleTitle;
    private String articleContent;
    private Long categoryId;
}
