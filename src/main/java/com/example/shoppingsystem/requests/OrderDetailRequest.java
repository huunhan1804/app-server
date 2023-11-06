package com.example.shoppingsystem.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailRequest {
    private long productId;
    private long productVariantId;
    private int quantity;
    private String price;
    private String subtotal;
}
