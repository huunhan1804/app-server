package com.example.shoppingsystem.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryStatDTO {
    private String categoryName;
    private int productCount;
    private BigDecimal totalSales;
}
