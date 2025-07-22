package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SupportArticleDTO {
    private Long articleId;
    private SupportCategoryDTO category;
    private String articleTitle;
    private String articleContent;
    private Boolean isVisible;
    private Integer viewCount;
}
