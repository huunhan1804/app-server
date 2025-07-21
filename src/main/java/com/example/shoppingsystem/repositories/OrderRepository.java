package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.OrderList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderList, Long> {
    List<OrderList> findAllByAccount_AccountId(Long accountId);

    @Query("SELECT o FROM OrderList o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<OrderList> findOrdersInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<OrderList> findTop10ByOrderByOrderDateDesc();

    @Query("SELECT o FROM OrderList o ORDER BY o.orderDate DESC")
    Page<OrderList> findTop10ByOrderByOrderDateDesc(Pageable pageable);
}
