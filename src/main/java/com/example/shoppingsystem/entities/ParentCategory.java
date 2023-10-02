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
@Table(name = "parent_category")
public class ParentCategory extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PARENT_CATEGORY_ID")
    private Long parentCategoryId;

    @Column(name = "PARENT_CATEGORY_NAME", nullable = false)
    private String parentCategoryName;

    @Column(name = "PARENT_CATEGORY_DESCRIPTION", columnDefinition = "TEXT")
    private String parentCategoryDescription;
}
