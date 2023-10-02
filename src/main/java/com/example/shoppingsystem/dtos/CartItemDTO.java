package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartItemDTO {
    private Long cart_item_id;
    private Long product_id;
    private Long product_variant_id;
    private int quantity;
    private String sub_total;
}
