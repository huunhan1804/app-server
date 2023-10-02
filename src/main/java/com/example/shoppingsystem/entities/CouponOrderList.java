package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "coupon_order_list")
public class CouponOrderList extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUPON_ORDER_ID")
    private Long couponOrderId;

    @ManyToOne
    @JoinColumn(name = "COUPON_ID", referencedColumnName = "COUPON_ID", nullable = false)
    private Coupon coupon;

    @ManyToOne
    @JoinColumn(name = "ORDER_ID", referencedColumnName = "ORDER_ID", nullable = false)
    private OrderList orderList;
}
