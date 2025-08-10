package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "support_article")
public class SupportArticle extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleId;

    @Column(name = "ARTICLE_TITLE", nullable = false)
    private String articleTitle;

    @Column(name = "ARTICLE_CONTENT", columnDefinition = "TEXT")
    private String articleContent;

    @Column(name = "IS_VISIBLE")
    private Boolean isVisible = true;

    @Column(name = "VIEW_COUNT")
    private Integer viewCount = 0;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUPPORT_CATEGORY_ID")
    private SupportCategory supportCategory;

    @Column(name = "article_images", columnDefinition = "json")
    private String articleImages;

    @Transient
    public List<String> getArticleImages() {
        try {
            if (articleImages == null || articleImages.isBlank()) return List.of();
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(articleImages, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    @Transient
    public void setArticleImages(List<String> images) {
        try {
            this.articleImages = (images == null)
                    ? null
                    : new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(images);
        } catch (Exception e) {
            this.articleImages = null;
        }
    }

}
