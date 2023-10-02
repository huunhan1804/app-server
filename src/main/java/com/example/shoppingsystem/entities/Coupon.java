package com.example.shoppingsystem.entities;

import com.example.shoppingsystem.enums.CouponType;
import com.example.shoppingsystem.enums.DiscountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "coupon")
public class Coupon extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUPON_ID")
    private Long couponId;

    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID", referencedColumnName = "CATEGORY_ID")
    private Category category;

    @Column(name = "DISCOUNT_VALUE", precision = 10, scale = 2, nullable = false)
    private BigDecimal discountValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "DISCOUNT_TYPE", nullable = false)
    private DiscountType discountType;

    @Column(name = "EXPIRY_DATE", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "IS_ACTIVATED", nullable = false)
    private Boolean isActivated;

    @Column(name = "REMAINING_QUANTITY")
    private Integer remainingQuantity;

    @Column(name = "MIN_PURCHASE_AMOUNT", precision = 10, scale = 2)
    private BigDecimal minPurchaseAmount;

    @Column(name = "MIN_QUANTITY")
    private Integer minQuantity;

    @Column(name = "MAX_QUANTITY")
    private Integer maxQuantity;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "COUPON_TYPE")
    private CouponType couponType;
}
