package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.OrderDTO;
import com.example.shoppingsystem.dtos.OrderDetailDTO;
import com.example.shoppingsystem.requests.CheckoutRequest;
import com.example.shoppingsystem.requests.OrderRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "Order")
public class OrderController {
    private final OrderService orderService;

    @Operation(
            summary = "Order",
            description = "Add order."
    )
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<OrderDTO>> addOrder(
            @RequestBody OrderRequest request
    ) {
        ApiResponse<OrderDTO> apiResponse = orderService.createOrder(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(
            summary = "Ordered",
            description = "Get Ordered."
    )
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getAllOrdered() {
        ApiResponse<List<OrderDTO>> apiResponse = orderService.getAllOrder();
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(
            summary = "Get detail order",
            description = "Get detail order."
    )
    @GetMapping("/detail/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderDetail(@Parameter(description = "Order ID") @PathVariable long orderId) {
        ApiResponse<OrderDTO> apiResponse = orderService.getOrderInformation(orderId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(
            summary = "Cancel order",
            description = "Cancel order."
    )
    @GetMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> cancelOrder(@Parameter(description = "Order ID") @PathVariable long orderId) {
        ApiResponse<List<OrderDTO>> apiResponse = orderService.cancelOrder(orderId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(
            summary = "Get detail order",
            description = "Get detail order."
    )
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<List<OrderDetailDTO>>> getOrderDetailCheckout(@RequestBody CheckoutRequest request) {
        ApiResponse<List<OrderDetailDTO>> apiResponse = orderService.getOrderDetailCheckout(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }
}
