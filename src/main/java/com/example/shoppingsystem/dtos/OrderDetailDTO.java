package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailDTO {
    private ProductInfoDTO productInfoDTO;
    private ProductVariantDTO productVariantDTO;
    private String price;
    private int quantity;
    private String sub_total;
}
