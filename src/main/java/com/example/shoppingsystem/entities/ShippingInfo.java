package com.example.shoppingsystem.entities;

import com.example.shoppingsystem.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingInfo extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SHIPPING_INFO_ID")
    private Long shippingInfoId;

    @ManyToOne
    @JoinColumn(name = "ORDER_ID", referencedColumnName = "ORDER_ID", nullable = false)
    private OrderList order;

    @Column(name = "SHIPPING_PROVIDER", nullable = false)
    private String shippingProvider;

    @Column(name = "TRACKING_NUMBER", nullable = false)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "DELIVERY_STATUS", nullable = false)
    private DeliveryStatus deliveryStatus;

    @Column(name = "DELIVERY_DATE")
    private LocalDateTime deliveryDate;
}
