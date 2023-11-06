package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.AccountInfoDTO;
import com.example.shoppingsystem.requests.AddToCartRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart")
public class CartController {
    private final CartService cartService;
//
//    @Operation(summary = "", description = "")
//    @GetMapping("all")
//    public ResponseEntity<ApiResponse<>>

    @Operation(
            summary = "Add to Cart",
            description = "Add a product or product variant to the shopping cart."
    )
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<AccountInfoDTO>> addToCart(
            @RequestBody AddToCartRequest request
    ) {
        ApiResponse<AccountInfoDTO> apiResponse = cartService.addToCart(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(
            summary = "Increase Cart Item Quantity",
            description = "Increase the quantity of a product or product variant in the shopping cart."
    )
    @PostMapping("/increase")
    public ResponseEntity<ApiResponse<AccountInfoDTO>> increaseCartItemQuantity(
            @RequestBody AddToCartRequest request
    ) {
        ApiResponse<AccountInfoDTO> apiResponse = cartService.increaseCartItemQuantity(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(
            summary = "Decrease Cart Item Quantity",
            description = "Decrease the quantity of a product or product variant in the shopping cart. If quantity reaches 0, the item will be removed."
    )
    @PostMapping("/decrease")
    public ResponseEntity<ApiResponse<AccountInfoDTO>> decreaseCartItemQuantity(
            @RequestBody AddToCartRequest request
    ) {
        ApiResponse<AccountInfoDTO> apiResponse = cartService.decreaseCartItemQuantity(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(
            summary = "Delete Cart Item",
            description = "Delete a cart item from the shopping cart by specifying its cart item ID."
    )
    @DeleteMapping("/delete/{cartItemId}")
    public ResponseEntity<ApiResponse<AccountInfoDTO>> deleteCartItem(
            @Parameter(description = "Cart Item ID") @PathVariable long cartItemId
    ) {
        ApiResponse<AccountInfoDTO> apiResponse = cartService.removeCartItem(cartItemId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }
}
