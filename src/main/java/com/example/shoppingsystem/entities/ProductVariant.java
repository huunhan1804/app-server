package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "product_variant")
public class ProductVariant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_VARIANT_ID")
    private Long productVariantId;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID", referencedColumnName = "PRODUCT_ID", nullable = false)
    private Product product;

    @Column(name = "PRODUCT_VARIANT_NAME", nullable = false)
    private String productVariantName;

    @Column(name = "LIST_PRICE", precision = 10, scale = 2, nullable = false)
    private BigDecimal listPrice;

    @Column(name = "SALE_PRICE", precision = 10, scale = 2, nullable = false)
    private BigDecimal salePrice;

    @Column(name = "INVENTORY_QUANTITY", nullable = false)
    private Integer inventoryQuantity;

    @Column(name = "DESIRED_QUANTITY", nullable = false)
    private Integer desiredQuantity;
}
