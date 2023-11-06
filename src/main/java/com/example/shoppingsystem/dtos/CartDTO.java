package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartDTO {
    private Long id;
    private Integer total_item;
    private List<CartItemDTO> cart_item;
}
