package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "support_category")
public class SupportCategory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SUPPORT_CATEGORY_ID")
    private Long supportCategoryId;

    @Column(name = "SUPPORT_CATEGORY_NAME", nullable = false)
    private String supportCategoryName;

    @Column(name = "SUPPORT_CATEGORY_DESCRIPTION")
    private String supportCategoryDescription;
}
