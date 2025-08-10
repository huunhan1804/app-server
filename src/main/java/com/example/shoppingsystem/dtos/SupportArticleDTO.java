package com.example.shoppingsystem.dtos;

import com.example.shoppingsystem.entities.SupportArticle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    private List<String> articleImages;

    public static SupportArticleDTO fromEntity(SupportArticle e) {
        SupportArticleDTO dto = new SupportArticleDTO();
        // ... map field khác
        dto.setArticleImages(e.getArticleImages());
        return dto;
    }
}
