package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.AccountInfoDTO;
import com.example.shoppingsystem.dtos.AgencyInfoDTO;
import com.example.shoppingsystem.dtos.OrderDTO;
import com.example.shoppingsystem.dtos.ProductInfoDTO;
import com.example.shoppingsystem.requests.*;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.responses.ShipmentResponse;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public interface AgencyService {
    ApiResponse<AgencyInfoDTO> getAgencyInfo();
    ApiResponse<ProductInfoDTO> createProduct(AddNewProductRequest request);
    ApiResponse<ProductInfoDTO> updateProduct(UpdateProductRequest request);
    ApiResponse<AgencyInfoDTO> deleteProduct(Long product_id);
    ApiResponse<OrderDTO> shipOrder(ShippingRequest request);
    ApiResponse<OrderDTO> completeOrder(CompleteOrderRequest request);
    ApiResponse<OrderDTO> confirmReturnOrder(Long order_id);
    ApiResponse<OrderDTO> confirmOrder(ConfirmOrderRequest request);
    ApiResponse<OrderDTO> cancelOrder(AgencyCancelOrderRequest request);
    ApiResponse<String> getOrderStatus(Long order_id);
    ApiResponse<List<OrderDTO>> getListOfOrdersByStatus(ListOrderByStatusRequest request);
//    ApiResponse<OrderDTO> sendOrderToShipping(Long orderId);
//    ApiResponse<OrderDTO> trackShippingStatus(String trackingCode);
    ApiResponse<ProductInfoDTO> disableSellingProduct(Long product_id);


    ApiResponse<List<ProductInfoDTO>> getListProductByStatus(String status_code);
}
