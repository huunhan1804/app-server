package com.example.shoppingsystem.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompleteOrderRequest {
    private Long orderId;
    private Long agencyId;
}
