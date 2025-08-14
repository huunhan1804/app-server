package com.example.shoppingsystem.requests;

import com.example.shoppingsystem.dtos.FeedbackDTO;
import com.example.shoppingsystem.dtos.ProductVariantDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddNewProductRequest {
    private String product_name;
    private String product_description;
    private long category_id;
    private int quantity_in_stock;
    private List<String> image_urls;
    private List<AddProductVariantsRequest> product_variant_list;
}
