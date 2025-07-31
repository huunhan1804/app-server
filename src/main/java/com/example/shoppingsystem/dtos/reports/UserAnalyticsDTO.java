package com.example.shoppingsystem.dtos.reports;

import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
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
