package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_item")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CART_ITEM_ID")
    private Long cartItemId;

    @ManyToOne
    @JoinColumn(name = "CART_ID", referencedColumnName = "CART_ID", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID", referencedColumnName = "PRODUCT_ID")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_VARIANT_ID", referencedColumnName = "PRODUCT_VARIANT_ID")
    private ProductVariant productVariant;

    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;
}
