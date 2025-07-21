package com.example.shoppingsystem.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentOrderDTO {
    private Long orderId;
    private String customerName;
    private String totalAmount;
    private String status;
    private LocalDateTime orderDate;
}
