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
<<<<<<< HEAD

    @Column(name = "article_images", columnDefinition = "json")
    private String articleImages;


=======
>>>>>>> parent of 4586147 (ok 2231)
}
