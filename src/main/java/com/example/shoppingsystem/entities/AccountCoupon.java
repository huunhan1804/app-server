package com.example.shoppingsystem.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account_coupon")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCoupon extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACCOUNT_COUPON_ID")
    private Long accountCouponId;

    @ManyToOne
    @JoinColumn(name = "COUPON_ID", referencedColumnName = "COUPON_ID")
    private Coupon coupon;

    @ManyToOne
    @JoinColumn(name = "ACCOUNT_ID", referencedColumnName = "ACCOUNT_ID")
    private Account account;
}
