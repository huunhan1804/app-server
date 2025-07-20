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

    // THÊM CÁC TRƯỜNG BẮT BUỘC THIẾU
    @Column(name = "COUPON_CODE", nullable = false, unique = true, length = 50)
    private String couponCode;

    @Column(name = "COUPON_NAME", nullable = false, length = 255)
    private String couponName;

    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID", referencedColumnName = "CATEGORY_ID")
    private Category category;

    // THÊM AGENCY_ID
    @ManyToOne
    @JoinColumn(name = "AGENCY_ID", referencedColumnName = "ACCOUNT_ID")
    private Account agency;

    @Column(name = "DISCOUNT_VALUE", precision = 10, scale = 2, nullable = false)
    private BigDecimal discountValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "DISCOUNT_TYPE", nullable = false)
    private DiscountType discountType;

    // THÊM CÁC TRƯỜNG KHÁC TỪ DATABASE
    @Column(name = "MIN_PURCHASE_AMOUNT", precision = 10, scale = 2)
    private BigDecimal minPurchaseAmount;

    @Column(name = "MAX_DISCOUNT_AMOUNT", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "USAGE_LIMIT")
    private Integer usageLimit;

    @Column(name = "USED_COUNT", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "START_DATE", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "EXPIRY_DATE", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "IS_ACTIVE", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "COUPON_TYPE")
    @Builder.Default
    private CouponType couponType = CouponType.DISCOUNT;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "TERMS_CONDITIONS", columnDefinition = "TEXT")
    private String termsConditions;

    // GIỮ LẠI CÁC TRƯỜNG CŨ
    @Column(name = "IS_ACTIVATED", nullable = false)
    private Boolean isActivated;

    @Column(name = "REMAINING_QUANTITY")
    private Integer remainingQuantity;

    @Column(name = "MIN_QUANTITY")
    private Integer minQuantity;

    @Column(name = "MAX_QUANTITY")
    private Integer maxQuantity;
}