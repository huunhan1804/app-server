// PlatformRevenueReportDTO.java
package com.example.shoppingsystem.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlatformRevenueReportDTO {
    private LocalDate reportDate;
    private BigDecimal totalTransactionRevenue;
    private BigDecimal agencyCommissionRevenue;
    private BigDecimal subscriptionRevenue;
    private BigDecimal premiumListingRevenue;
    private BigDecimal operatingCosts;
    private BigDecimal netProfit;

    // Constructor với các phương thức tính toán
    public BigDecimal getTotalRevenue() {
        return totalTransactionRevenue.add(agencyCommissionRevenue)
                .add(subscriptionRevenue).add(premiumListingRevenue);
    }

    public BigDecimal getGrossProfit() {
        return getTotalRevenue().subtract(operatingCosts);
    }
}
