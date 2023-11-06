package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductInfoDTO {
    private Long product_id;
    private String product_name;
    private String product_price;
    private String product_description;
    private double rating;
    private int quantity_in_stock;
    private List<String> media_url;
    private List<ProductVariantDTO> product_variant_list;
    private List<FeedbackDTO> feedback_list;
}
