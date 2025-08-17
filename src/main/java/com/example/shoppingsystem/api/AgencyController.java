package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.*;
import com.example.shoppingsystem.requests.*;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.AgencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agency/")
@RequiredArgsConstructor
@PreAuthorize("hasRole('AGENCY')")
@Tag(name = "Agency")
public class AgencyController {
    private final AgencyService agencyService;

    @Operation(summary = "Get products by status", description = "Get list of product by status")
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductInfoDTO>>> getProductsByStatus(
            @Parameter(description = "Status Code (APPROVED, PENDING, REJECTED)")
            @RequestParam(name = "status_code", required = false) String statusCode
    ) {
        ApiResponse<List<ProductInfoDTO>> response = agencyService.getListProductByStatus(statusCode);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Get product details", description = "Get full details of a single product")
    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductFullDTO>> getProductDetails(
            @Parameter(description = "Product ID")
            @PathVariable Long productId
    ) {
        ApiResponse<ProductFullDTO> response = agencyService.getFullInfoProduct(productId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Agency Add Product", description = "Agency Add New Product")
    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductFullDTO>> createProduct(
            @RequestBody AddNewProductRequest request
    ) {
        ApiResponse<ProductFullDTO> response = agencyService.createProduct(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Agency Update Product", description = "Agency Update Product")
    @PostMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductFullDTO>> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @RequestBody UpdateProductRequest request
    ) {
        request.setProduct_id(productId);
        ApiResponse<ProductFullDTO> response = agencyService.updateProduct(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Agency Delete Product", description = "Agency Delete Product")
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId
    ) {
        ApiResponse<Void> response = agencyService.deleteProduct(productId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Toggle product sale status", description = "Toggle product sale status")
    @PostMapping("/products/{productId}/toggle-sale-status")
    public ResponseEntity<ApiResponse<ProductInfoDTO>> toggleSellingProduct(
            @PathVariable Long productId
    ) {
        ApiResponse<ProductInfoDTO> response = agencyService.toggleProductSaleStatus(productId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Get list of order", description = "Get list of order")
    @PostMapping("/order")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrders(){
        ApiResponse<List<OrderDTO>> response = agencyService.getOrders();
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

    @Operation(summary = "Return order", description = "Return order")
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
