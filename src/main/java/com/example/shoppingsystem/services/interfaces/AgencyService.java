package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.AccountInfoDTO;
import com.example.shoppingsystem.dtos.OrderDTO;
import com.example.shoppingsystem.dtos.ProductInfoDTO;
import com.example.shoppingsystem.requests.AddNewProductRequest;
import com.example.shoppingsystem.requests.ConfirmOrderRequest;
import com.example.shoppingsystem.requests.ListOrderByStatusRequest;
import com.example.shoppingsystem.requests.UpdateProductRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.responses.ShipmentResponse;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public interface AgencyService {
    ApiResponse<ProductInfoDTO> createProduct(AddNewProductRequest request);
    ApiResponse<ProductInfoDTO> updateProduct(UpdateProductRequest request);
    ApiResponse<AccountInfoDTO> deleteProduct(Long product_id);
    ShipmentResponse shipOrder(Long order_id, String agency_email) throws AccessDeniedException;
    ApiResponse<OrderDTO> confirmOrder(ConfirmOrderRequest request);
    ApiResponse<String> getOrderStatus(Long order_id);
    ApiResponse<List<OrderDTO>> getListOfOrdersByStatus(ListOrderByStatusRequest request);
//    ApiResponse<OrderDTO> sendOrderToShipping(Long orderId);
//    ApiResponse<OrderDTO> trackShippingStatus(String trackingCode);


    ApiResponse<List<ProductInfoDTO>> getListProductByStatus(String status_code);
}
