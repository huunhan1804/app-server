package com.example.shoppingsystem.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductVariantsRequest {
    private Long product_variant_id;
    private String product_variant_name;
    private Double list_price;
    private Double sale_price;
    private int inventory_quantity;
    private int sold_amount;
}
