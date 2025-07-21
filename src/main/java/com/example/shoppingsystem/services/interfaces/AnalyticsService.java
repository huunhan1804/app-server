package com.example.shoppingsystem.services.interfaces;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
public interface AnalyticsService {
    void trackPageView(String page, String userId, String deviceType, String location);
    void trackUserSession(String userId, Long sessionDuration, String deviceType);
    void trackConversion(String userId, String productId, String conversionType);

    Map<String, Object> getPageViewStatistics(LocalDate startDate, LocalDate endDate);
    Map<String, Object> getSessionStatistics(LocalDate startDate, LocalDate endDate);
    Map<String, Object> getConversionStatistics(LocalDate startDate, LocalDate endDate);
}
