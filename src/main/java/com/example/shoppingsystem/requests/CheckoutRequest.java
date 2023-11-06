package com.example.shoppingsystem.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {
    private List<Long> cart_item_ids;
}
