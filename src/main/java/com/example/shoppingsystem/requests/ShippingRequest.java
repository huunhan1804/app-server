package com.example.shoppingsystem.requests;

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
public class ShippingRequest {
    private String order_code;
    private String receiver_name;
    private String receiver_phone;
    private String receiver_address;
    private BigDecimal total_amount;
    private List<ShippingItem> items;

}

