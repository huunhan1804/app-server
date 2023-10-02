package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantDTO {
    private Long product_variant_id;
    private String product_variant_name;
    private String product_variant_image_url;
    private String origin_price;
    private String sale_price;
    private int quantity_in_stock;
}
