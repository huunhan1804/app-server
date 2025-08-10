package com.example.shoppingsystem.requests;

import lombok.Data;

import java.util.List;

@Data
public class UpdateArticleRequest {
    private List<String> articleImages;
    private String articleTitle;
    private String articleContent;
    private Long categoryId;
}
