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
    private String product_description;
    private long category_id;
    private List<String> image_urls;
    private List<UpdateProductVariantsRequest> product_variant_list;
    private int quantity_in_stock;
}
