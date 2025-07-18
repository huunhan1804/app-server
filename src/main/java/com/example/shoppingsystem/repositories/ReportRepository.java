package com.example.shoppingsystem.repositories;


import com.example.shoppingsystem.entities.ReportData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportData, Long> {

    // Annual Product Report
    @Query(value = """
        SELECT 
            YEAR(o.ORDER_DATE) as period,
            COUNT(DISTINCT od.PRODUCT_ID) as totalProducts,
            SUM(od.TOTAL_PRICE) as totalRevenue,
            SUM(od.QUANTITY) as totalOrders,
            p.PRODUCT_NAME as productName,
            c.CATEGORY_NAME as categoryName
        FROM order_list o 
        JOIN order_detail od ON o.ORDER_ID = od.ORDER_ID
        JOIN product p ON od.PRODUCT_ID = p.PRODUCT_ID
        JOIN category c ON p.CATEGORY_ID = c.CATEGORY_ID
        WHERE YEAR(o.ORDER_DATE) = :year 
        AND o.ORDER_STATUS = 'DELIVERED'
        GROUP BY YEAR(o.ORDER_DATE), p.PRODUCT_ID, p.PRODUCT_NAME, c.CATEGORY_NAME
        ORDER BY SUM(od.TOTAL_PRICE) DESC
        """, nativeQuery = true)
    List<Object[]> generateAnnualProductReport(@Param("year") int year);

    // Quarterly Agency Report
    @Query(value = """
        SELECT 
            CONCAT(YEAR(o.ORDER_DATE), '-Q', QUARTER(o.ORDER_DATE)) as period,
            ai.BUSINESS_NAME as agencyName,
            COUNT(o.ORDER_ID) as totalOrders,
            SUM(o.FINAL_AMOUNT) as totalRevenue,
            AVG(o.FINAL_AMOUNT) as averageOrderValue,
            COUNT(DISTINCT o.ACCOUNT_ID) as totalCustomers
        FROM order_list o
        JOIN account a ON o.AGENCY_ID = a.ACCOUNT_ID
        JOIN agency_info ai ON a.ACCOUNT_ID = ai.ACCOUNT_ID
        WHERE YEAR(o.ORDER_DATE) = :year 
        AND QUARTER(o.ORDER_DATE) = :quarter
        AND o.ORDER_STATUS = 'DELIVERED'
        GROUP BY CONCAT(YEAR(o.ORDER_DATE), '-Q', QUARTER(o.ORDER_DATE)), ai.BUSINESS_NAME
        ORDER BY SUM(o.FINAL_AMOUNT) DESC
        """, nativeQuery = true)
    List<Object[]> generateQuarterlyAgencyReport(@Param("year") int year, @Param("quarter") int quarter);

    // Monthly Sales Report
    @Query(value = """
        SELECT 
            DATE_FORMAT(o.ORDER_DATE, '%Y-%m') as period,
            COUNT(o.ORDER_ID) as totalOrders,
            SUM(o.FINAL_AMOUNT) as totalRevenue,
            AVG(o.FINAL_AMOUNT) as averageOrderValue
        FROM order_list o
        WHERE DATE(o.ORDER_DATE) BETWEEN :startDate AND :endDate
        AND o.ORDER_STATUS = 'DELIVERED'
        GROUP BY DATE_FORMAT(o.ORDER_DATE, '%Y-%m')
        ORDER BY period DESC
        """, nativeQuery = true)
    List<Object[]> generateMonthlySalesReport(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Revenue Analytics Report
    @Query(value = """
        SELECT 
            DATE(o.ORDER_DATE) as period,
            COUNT(o.ORDER_ID) as totalOrders,
            SUM(o.FINAL_AMOUNT) as totalRevenue,
            AVG(o.FINAL_AMOUNT) as averageOrderValue,
            SUM(o.SHIPPING_FEE) as totalShippingFee,
            SUM(o.INSURANCE_FEE) as totalInsuranceFee,
            SUM(o.DISCOUNT_AMOUNT) as totalDiscounts
        FROM order_list o
        WHERE DATE(o.ORDER_DATE) BETWEEN :startDate AND :endDate
        AND o.ORDER_STATUS = 'DELIVERED'
        GROUP BY DATE(o.ORDER_DATE)
        ORDER BY DATE(o.ORDER_DATE) DESC
        """, nativeQuery = true)
    List<Object[]> createRevenueAnalyticsReport(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Sales Performance Tracking
    @Query(value = """
        SELECT 
            ai.BUSINESS_NAME as agencyName,
            COUNT(o.ORDER_ID) as totalOrders,
            SUM(o.FINAL_AMOUNT) as totalRevenue,
            AVG(o.FINAL_AMOUNT) as averageOrderValue,
            ai.STORE_RATING as rating,
            ai.TOTAL_REVIEWS as totalReviews,
            COUNT(DISTINCT p.PRODUCT_ID) as totalProducts
        FROM agency_info ai
        LEFT JOIN account a ON ai.ACCOUNT_ID = a.ACCOUNT_ID
        LEFT JOIN order_list o ON a.ACCOUNT_ID = o.AGENCY_ID AND o.ORDER_STATUS = 'DELIVERED'
        LEFT JOIN product p ON a.ACCOUNT_ID = p.ACCOUNT_ID AND p.PRODUCT_STATUS = 'ACTIVE'
        WHERE DATE(o.ORDER_DATE) BETWEEN :startDate AND :endDate
        OR o.ORDER_DATE IS NULL
        GROUP BY ai.AGENCY_INFO_ID, ai.BUSINESS_NAME, ai.STORE_RATING, ai.TOTAL_REVIEWS
        ORDER BY SUM(o.FINAL_AMOUNT) DESC
        """, nativeQuery = true)
    List<Object[]> trackSalesPerformance(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}