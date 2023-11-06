package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartItemDTO {
    private Long cart_item_id;
    private ProductInfoDTO product_info;
    private ProductVariantDTO product_variant_info;
    private int quantity;
    private String sub_total;
}
