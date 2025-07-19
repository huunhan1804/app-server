// RevenueDataDTO.java
package com.example.shoppingsystem.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueDataDTO {
    private String date;
    private BigDecimal revenue;
}
