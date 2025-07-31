package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByIsActivatedTrue();
    List<Coupon> findByExpiryDateAfter(LocalDateTime date);
    List<Coupon> findByCategory_CategoryId(Long categoryId);
}