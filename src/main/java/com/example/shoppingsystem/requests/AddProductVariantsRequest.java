package com.example.shoppingsystem.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddProductVariantsRequest {
    private Long product_variant_id;
    private String product_variant_name;
    //private String product_variant_image_url;
    private String product_list_price;
    private String product_sale_price;
    private int inventory_quantity;
    private int sold_amount;
}
