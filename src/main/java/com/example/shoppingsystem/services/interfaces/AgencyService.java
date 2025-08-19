package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.*;
import com.example.shoppingsystem.requests.*;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.responses.ShipmentResponse;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public interface AgencyService {
    ApiResponse<AgencyInfoDTO> getAgencyInfo();
    ApiResponse<ProductFullDTO> createProduct(AddNewProductRequest request);
    ApiResponse<ProductFullDTO> updateProduct(UpdateProductRequest request);
    ApiResponse<Void> deleteProduct(Long product_id);
    ApiResponse<List<OrderDTO>> getOrders();
    ApiResponse<OrderDTO> shipOrder(ShippingRequest request);
    ApiResponse<OrderDTO> completeOrder(CompleteOrderRequest request);
    ApiResponse<OrderDTO> confirmReturnOrder(Long order_id);
    ApiResponse<OrderDTO> confirmOrder(ConfirmOrderRequest request);
    ApiResponse<OrderDTO> cancelOrder(AgencyCancelOrderRequest request);
    ApiResponse<String> getOrderStatus(Long order_id);
    ApiResponse<List<OrderDTO>> getListOfOrdersByStatus(String status);
    //    ApiResponse<OrderDTO> sendOrderToShipping(Long orderId);
//    ApiResponse<OrderDTO> trackShippingStatus(String trackingCode);
//    ApiResponse<ProductInfoDTO> disableSellingProduct(Long product_id);
    ApiResponse<List<ProductInfoDTO>> getListProductByStatus(String status_code);
    ApiResponse<ProductFullDTO> getFullInfoProduct(long productId);
    ApiResponse<ProductInfoDTO> toggleProductSaleStatus(Long productId);
    ApiResponse<ShopInfoDTO> getShopInfo();
    ApiResponse<OrderDTO> respondToReturnRequest(AgencyReturnResponseRequest request);
}
