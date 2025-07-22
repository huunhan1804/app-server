package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "support_article")
public class SupportArticle extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ARTICLE_ID")
    private Long articleId;

    @ManyToOne
    @JoinColumn(name = "SUPPORT_CATEGORY_ID", nullable = false)
    private SupportCategory supportCategory;

    @Column(name = "ARTICLE_TITLE", nullable = false)
    private String articleTitle;

    @Column(name = "ARTICLE_CONTENT", nullable = false)
    private String articleContent;

    @Column(name = "IS_VISIBLE")
    private Boolean isVisible = true;

    @Column(name = "VIEW_COUNT")
    private Integer viewCount = 0;

}
