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
public class OrderRequest {
    private String address_detail;
    private String total_bill;
    private List<OrderDetailRequest> order_items;
}
