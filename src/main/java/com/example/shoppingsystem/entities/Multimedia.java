package com.example.shoppingsystem.entities;

import com.example.shoppingsystem.enums.MultimediaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "multimedia")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Multimedia extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MULTIMEDIA_ID")
    private Long multimediaId;

    @ManyToOne
    @JoinColumn(name = "ACCOUNT_ID", referencedColumnName = "ACCOUNT_ID")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID", referencedColumnName = "PRODUCT_ID")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_VARIANT_ID", referencedColumnName = "PRODUCT_VARIANT_ID")
    private ProductVariant productVariant;

    @ManyToOne
    @JoinColumn(name = "FEEDBACK_ID", referencedColumnName = "FEEDBACK_ID")
    private Feedback feedback;

    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID", referencedColumnName = "CATEGORY_ID")
    private Category category;

    @Column(name = "MULTIMEDIA_URL", nullable = false)
    private String multimediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "MULTIMEDIA_TYPE", nullable = false)
    private MultimediaType multimediaType;

}
