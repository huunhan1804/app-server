package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.CouponOrderList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponOrderListRepository extends JpaRepository<CouponOrderList, Long> {
    List<CouponOrderList> findByOrderList_OrderId(Long orderId);
    List<CouponOrderList> findByCoupon_CouponId(Long couponId);
}