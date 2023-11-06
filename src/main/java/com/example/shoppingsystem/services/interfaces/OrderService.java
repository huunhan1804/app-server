package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.OrderDTO;
import com.example.shoppingsystem.dtos.OrderDetailDTO;
import com.example.shoppingsystem.requests.CheckoutRequest;
import com.example.shoppingsystem.requests.OrderRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface OrderService {
    ApiResponse<List<OrderDTO>> getAllOrder();
    ApiResponse<OrderDTO> getOrderInformation(Long orderId);
    ApiResponse<OrderDTO> createOrder(OrderRequest orderRequest);
    ApiResponse<List<OrderDetailDTO>> getOrderDetailCheckout(CheckoutRequest request);
    ApiResponse<List<OrderDTO>> cancelOrder(long orderId);
}
