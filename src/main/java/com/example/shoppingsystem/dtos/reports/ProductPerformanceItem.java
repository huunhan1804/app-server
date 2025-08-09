package com.example.shoppingsystem.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductPerformanceItem {
    private String productName;
    private int quantitySold;
    private BigDecimal revenue;
}
