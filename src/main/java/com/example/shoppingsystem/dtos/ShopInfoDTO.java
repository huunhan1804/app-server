package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ShopInfoDTO {
    private long shopId;
    private String shopName;
    private String shopAddress;
    private String shopPhone;
    private String shopEmail;
    private String shopAvatar;
    //private List<ProductInfoDTO> products;
}
