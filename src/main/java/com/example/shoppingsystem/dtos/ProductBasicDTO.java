package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductBasicDTO {
    private Long product_id;
    private String image_url;
    private String product_name;
    private String product_price;
    private double rating;
}
