package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    @Query(value = "SELECT PRODUCT_ID, SUM(QUANTITY) as TOTAL_QUANTITY FROM order_detail GROUP BY PRODUCT_ID ORDER BY TOTAL_QUANTITY DESC LIMIT 10", nativeQuery = true)
    List<Object[]> findBestOrderProducts();

    List<OrderDetail> findAllByOrderList_OrderId(Long orderId);
}
