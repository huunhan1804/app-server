package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.AccountCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountCouponRepository extends JpaRepository<AccountCoupon, Long> {
    List<AccountCoupon> findByAccount_AccountId(Long accountId);
    List<AccountCoupon> findByCoupon_CouponId(Long couponId);
}