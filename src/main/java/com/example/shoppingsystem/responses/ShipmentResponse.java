package com.example.shoppingsystem.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentResponse {
    private String trackingId;
    private String carrier;
    private String estimatedDeliveryDate;
    private String status;
}
