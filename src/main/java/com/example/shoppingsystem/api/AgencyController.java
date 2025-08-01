package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.AccountInfoDTO;
import com.example.shoppingsystem.dtos.AgencyInfoDTO;
import com.example.shoppingsystem.dtos.OrderDTO;
import com.example.shoppingsystem.dtos.ProductInfoDTO;
import com.example.shoppingsystem.requests.*;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.AgencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agency/")
@RequiredArgsConstructor
@Tag(name = "Agency")
public class AgencyController {
    private final AgencyService agencyService;

    @Operation(summary = "Agency Add Product", description = "Agency Add New Product")
    @PostMapping("/product/add")
    public ResponseEntity<ApiResponse<ProductInfoDTO>> createProduct(
            @RequestBody AddNewProductRequest request
    ) {
        ApiResponse<ProductInfoDTO> response = agencyService.createProduct(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Agency Update Product", description = "Agency Update Product")
    @PostMapping("/product/update")
    public ResponseEntity<ApiResponse<ProductInfoDTO>> updateProduct(
            @RequestBody UpdateProductRequest request
    ) {
        ApiResponse<ProductInfoDTO> response = agencyService.updateProduct(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Agency Delete Product", description = "Agency Delete Product")
    @PostMapping("/product/delete/{product_id}")
    public ResponseEntity<ApiResponse<AgencyInfoDTO>> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable long product_id
    ) {
        ApiResponse<AgencyInfoDTO> response = agencyService.deleteProduct(product_id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Disable product", description = "Disable product")
    @PostMapping("/product/disable/{product_id}")
    public ResponseEntity<ApiResponse<ProductInfoDTO>> disableSellingProduct(
            @PathVariable Long product_id
    ){
        ApiResponse<ProductInfoDTO> response = agencyService.disableSellingProduct(product_id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Get products by status", description = "Get list of product by status")
    @PostMapping("/product/status/{status_code}")
    public ResponseEntity<ApiResponse<List<ProductInfoDTO>>> getProductsByStatus(
            @Parameter(description = "Status Code") @PathVariable String status_code
    ){
        ApiResponse<List<ProductInfoDTO>> response = agencyService.getListProductByStatus(status_code);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Confirm order", description = "Confirm order")
    @PostMapping("/order/confirm")
    public ResponseEntity<ApiResponse<OrderDTO>> confirmOrder(
            @RequestBody ConfirmOrderRequest request
    ){
        ApiResponse<OrderDTO> response = agencyService.confirmOrder(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Get order status", description = "Get order status")
    @PostMapping("/order/status/{order_id}")
    public ResponseEntity<ApiResponse<String>> getOrderStatus(
            @Parameter(description = "Order id") @PathVariable Long order_id
    ){
        ApiResponse<String> response = agencyService.getOrderStatus(order_id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Get list of order by status", description = "Get list of order by status")
    @PostMapping("/order/all-order-by-status")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getListOrderByStatus(
            @RequestBody ListOrderByStatusRequest request
    ){
        ApiResponse<List<OrderDTO>> response = agencyService.getListOfOrdersByStatus(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Agency cancel order", description = "Agency cancel order")
    @PostMapping("/order/cancel")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @RequestBody AgencyCancelOrderRequest request
    ){
        ApiResponse<OrderDTO> response = agencyService.cancelOrder(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Complete order", description = "Complete order")
    @PostMapping("/order/complete")
    public ResponseEntity<ApiResponse<OrderDTO>> completeOrder(
            @RequestBody CompleteOrderRequest request
    ){
        ApiResponse<OrderDTO> response = agencyService.completeOrder(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Shipping order", description = "Shipping order")
    @PostMapping("/order/shipping")
    public ResponseEntity<ApiResponse<OrderDTO>> shipOrder(
            @RequestBody ShippingRequest request
    ){
        ApiResponse<OrderDTO> response = agencyService.shipOrder(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Shipping order", description = "Shipping order")
    @PostMapping("/order/return/{order_id}")
    public ResponseEntity<ApiResponse<OrderDTO>> confirmReturnOrder(
            @PathVariable Long order_id
    ){
        ApiResponse<OrderDTO> response = agencyService.confirmReturnOrder(order_id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Get Agency information", description = "Get agency information")
    @PostMapping("/info")
    public ResponseEntity<ApiResponse<AgencyInfoDTO>> getAgencyInfo(){
        ApiResponse<AgencyInfoDTO> response = agencyService.getAgencyInfo();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}
