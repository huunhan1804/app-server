package com.example.shoppingsystem.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductRequest {
    private long product_id;
    private String product_name;
    private String product_list_price;
    private String product_sale_price;
    private String product_description;
    private int quantity_in_stock;
    private long category_id;
    private String image_url;
    private String product_safety_certificate_url;
    private List<AddProductVariantsRequest> product_variant_list;
}
