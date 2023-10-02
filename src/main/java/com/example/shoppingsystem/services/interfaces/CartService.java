package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.AccountInfoDTO;
import com.example.shoppingsystem.requests.AddToCartRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import org.springframework.stereotype.Service;
@Service
public interface CartService {
    ApiResponse<AccountInfoDTO> addToCart(AddToCartRequest request);
    ApiResponse<AccountInfoDTO> increaseCartItemQuantity(AddToCartRequest request);
    ApiResponse<AccountInfoDTO> decreaseCartItemQuantity(AddToCartRequest request);
    ApiResponse<AccountInfoDTO> removeCartItem(long cartItemId);
}
