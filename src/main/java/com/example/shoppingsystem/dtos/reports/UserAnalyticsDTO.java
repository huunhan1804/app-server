package com.example.shoppingsystem.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAnalyticsDTO {
    private String metric;
    private Long totalSessions;
    private Long totalPageViews;
    private BigDecimal averageSessionDuration;
    private BigDecimal bounceRate;
    private BigDecimal conversionRate;
    private String deviceType;
    private String location;
}
