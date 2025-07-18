package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "shop")
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SHOP_ID")
    private Long shopId;

    @Column(name = "SHOP_NAME")
    private String shopName;

    // thêm field khác nếu bạn có
}
