package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.OrderList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface OrderRepository extends JpaRepository<OrderList, Long> {
    List<OrderList> findAllByAccount_AccountId(Long accountId);
    @Query("SELECT COALESCE(SUM(ol.totalPrice), 0) FROM OrderList ol WHERE ol.orderStatus = 'DELIVERED' AND MONTH(ol.orderDate) = MONTH(CURRENT_DATE) AND YEAR(ol.orderDate) = YEAR(CURRENT_DATE)")
    BigDecimal getMonthlyRevenue();

    @Query("SELECT COUNT(ol) FROM OrderList ol WHERE DATE(ol.orderDate) = CURRENT_DATE")
    Long getTodayOrdersCount();

    @Query("SELECT ol FROM OrderList ol ORDER BY ol.orderDate DESC")
    List<OrderList> getRecentOrders(int limit);

    @Query("SELECT COALESCE(SUM(ol.totalPrice), 0) FROM OrderList ol WHERE ol.orderStatus = 'DELIVERED' AND DATE(ol.orderDate) = :date")
    BigDecimal getSalesByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(ol) FROM OrderList ol WHERE DATE(ol.orderDate) = :date")
    Long getOrderCountByDate(@Param("date") LocalDate date);

    @Query("SELECT " +
            "(COUNT(ol) - (SELECT COUNT(ol2) FROM OrderList ol2 WHERE MONTH(ol2.orderDate) = MONTH(CURRENT_DATE) - 1 AND YEAR(ol2.orderDate) = YEAR(CURRENT_DATE))) / " +
            "(SELECT COUNT(ol3) FROM OrderList ol3 WHERE MONTH(ol3.orderDate) = MONTH(CURRENT_DATE) - 1 AND YEAR(ol3.orderDate) = YEAR(CURRENT_DATE)) * 100 " +
            "FROM OrderList ol WHERE MONTH(ol.orderDate) = MONTH(CURRENT_DATE) AND YEAR(ol.orderDate) = YEAR(CURRENT_DATE)")
    Double getOrdersGrowthPercentage();

    @Query("SELECT " +
            "(SUM(ol.finalAmount) - (SELECT SUM(ol2.finalAmount) FROM OrderList ol2 WHERE MONTH(ol2.orderDate) = MONTH(CURRENT_DATE) - 1 AND YEAR(ol2.orderDate) = YEAR(CURRENT_DATE))) / " +
            "(SELECT SUM(ol3.finalAmount) FROM OrderList ol3 WHERE MONTH(ol3.orderDate) = MONTH(CURRENT_DATE) - 1 AND YEAR(ol3.orderDate) = YEAR(CURRENT_DATE)) * 100 " +
            "FROM OrderList ol WHERE MONTH(ol.orderDate) = MONTH(CURRENT_DATE) AND YEAR(ol.orderDate) = YEAR(CURRENT_DATE)")
    Double getRevenueGrowthPercentage();
}
