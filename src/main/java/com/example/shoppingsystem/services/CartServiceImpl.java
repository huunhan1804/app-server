package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.Message;
import com.example.shoppingsystem.dtos.AccountInfoDTO;
import com.example.shoppingsystem.entities.*;
import com.example.shoppingsystem.repositories.CartItemRepository;
import com.example.shoppingsystem.repositories.CartRepository;
import com.example.shoppingsystem.requests.AddToCartRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.AccountService;
import com.example.shoppingsystem.services.interfaces.CartService;
import com.example.shoppingsystem.services.interfaces.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AccountService accountService;
    private final ProductService productService;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository, AccountService accountService, ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.accountService = accountService;
        this.productService = productService;
    }

    @Override
    public ApiResponse<AccountInfoDTO> addToCart(AddToCartRequest request) {
        Optional<Account> account = accountService.findCurrentUserInfo();
        ApiResponse<AccountInfoDTO> response;

        if (account.isEmpty()) {
            response = createErrorResponse(ErrorCode.FORBIDDEN, Message.ACCOUNT_NOT_FOUND);
        } else {
            Optional<Cart> cart = cartRepository.findByAccount_AccountId(account.get().getAccountId());
            if (cart.isEmpty()) {
                response = createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, Message.CART_NOT_FOUND);
            } else {
                Optional<Product> product = Optional.empty();
                Optional<ProductVariant> productVariant = Optional.empty();

                if (request.getProduct_id() != 0) {
                    product = productService.getProductByID(request.getProduct_id());
                }

                if (request.getProduct_variant_id() != 0) {
                    productVariant = productService.getProductVariantByID(request.getProduct_variant_id());
                }

                if (product.isEmpty() || productVariant.isEmpty()) {
                    response = createErrorResponse(ErrorCode.NOT_FOUND, Message.PRODUCT_NOT_FOUND);
                } else {
                    response = processCartUpdate(cart.get(), product, productVariant, request.getQuantity());
                }
            }
        }

        return response;
    }

    private ApiResponse<AccountInfoDTO> createErrorResponse(int errorCode, String message) {
        return ApiResponse.<AccountInfoDTO>builder()
                .status(errorCode)
                .message(message)
                .timestamp(new Date())
                .build();
    }

    private ApiResponse<AccountInfoDTO> processCartUpdate(Cart cart, Optional<Product> product, Optional<ProductVariant> productVariant, int quantity) {
        int desiredQuantity = productVariant.isPresent() ? productVariant.get().getDesiredQuantity() : product.get().getDesiredQuantity();

        if (desiredQuantity != 0 && quantity <= desiredQuantity) {
            Optional<CartItem> existingCartItem = findCartItemInCart(cart, product, productVariant);

            if (existingCartItem.isPresent()) {
                CartItem cartItemToUpdate = existingCartItem.get();
                int newQuantity = cartItemToUpdate.getQuantity() + quantity;
                if (newQuantity <= 0) {
                    cartItemRepository.delete(cartItemToUpdate);
                } else {
                    if (newQuantity <= desiredQuantity) {
                        cartItemToUpdate.setQuantity(newQuantity);
                        cartItemRepository.save(cartItemToUpdate);
                    } else {
                        return createErrorResponse(ErrorCode.BAD_REQUEST, Message.NOT_ENOUGH_QUANTITY);
                    }

                }
            } else {
                if (quantity > 0) {
                    CartItem cartItem = new CartItem();
                    cartItem.setCart(cart);
                    cartItem.setQuantity(quantity);

                    product.ifPresent(cartItem::setProduct);
                    productVariant.ifPresent(cartItem::setProductVariant);

                    cartItemRepository.save(cartItem);
                }
            }

            List<CartItem> cartList = cartItemRepository.findAllByCart_CartId(cart.getCartId());
            cart.setTotalItem(cartList.size());
            cartRepository.save(cart);

            return accountService.getCurrentUserInfo();
        } else {
            return createErrorResponse(ErrorCode.BAD_REQUEST, Message.NOT_ENOUGH_QUANTITY);
        }
    }

    private Optional<CartItem> findCartItemInCart(Cart cart, Optional<Product> product, Optional<ProductVariant> productVariant) {
        if (product.isPresent() && productVariant.isPresent()) {
            return cartItemRepository.findByCartAndProductAndProductVariant(cart, product.get(), productVariant.get());
        }
        return Optional.empty();
    }

    @Override
    public ApiResponse<AccountInfoDTO> increaseCartItemQuantity(AddToCartRequest request) {
        Optional<Account> account = accountService.findCurrentUserInfo();

        if (account.isEmpty()) {
            return createErrorResponse(ErrorCode.FORBIDDEN, Message.ACCOUNT_NOT_FOUND);
        }

        Optional<Cart> cart = cartRepository.findByAccount_AccountId(account.get().getAccountId());

        if (cart.isEmpty()) {
            return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, Message.CART_NOT_FOUND);
        }

        Optional<Product> product = Optional.empty();
        Optional<ProductVariant> productVariant = Optional.empty();

        if (request.getProduct_id() != 0) {
            product = productService.getProductByID(request.getProduct_id());
        }

        if (request.getProduct_variant_id() != 0) {
            productVariant = productService.getProductVariantByID(request.getProduct_variant_id());
        }

        if (product.isEmpty() && productVariant.isEmpty()) {
            return createErrorResponse(ErrorCode.NOT_FOUND, Message.PRODUCT_NOT_FOUND);
        }

        int desiredQuantity = productVariant.isPresent() ? productVariant.get().getDesiredQuantity() : product.get().getDesiredQuantity();

        if (desiredQuantity == 0 || request.getQuantity() > desiredQuantity) {
            return createErrorResponse(ErrorCode.BAD_REQUEST, Message.NOT_ENOUGH_QUANTITY);
        }

        ApiResponse<AccountInfoDTO> response = processCartUpdate(cart.get(), product, productVariant, request.getQuantity());

        return response;
    }

    @Override
    public ApiResponse<AccountInfoDTO> decreaseCartItemQuantity(AddToCartRequest request) {
        Optional<Account> account = accountService.findCurrentUserInfo();

        if (!account.isPresent()) {
            return createErrorResponse(ErrorCode.FORBIDDEN, Message.ACCOUNT_NOT_FOUND);
        }

        Optional<Cart> cart = cartRepository.findByAccount_AccountId(account.get().getAccountId());

        if (!cart.isPresent()) {
            return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, Message.CART_NOT_FOUND);
        }

        Optional<Product> product = Optional.empty();
        Optional<ProductVariant> productVariant = Optional.empty();

        if (request.getProduct_id() != 0) {
            product = productService.getProductByID(request.getProduct_id());
        }

        if (request.getProduct_variant_id() != 0) {
            productVariant = productService.getProductVariantByID(request.getProduct_variant_id());
        }

        if (product.isEmpty() && productVariant.isEmpty()) {
            return createErrorResponse(ErrorCode.NOT_FOUND, Message.PRODUCT_NOT_FOUND);
        }

        int desiredQuantity = productVariant.isPresent() ? productVariant.get().getDesiredQuantity() : product.get().getDesiredQuantity();

        if (desiredQuantity == 0) {
            return createErrorResponse(ErrorCode.BAD_REQUEST, Message.NOT_ENOUGH_QUANTITY);
        }

        ApiResponse<AccountInfoDTO> response = processCartUpdate(cart.get(), product, productVariant, -request.getQuantity());

        return response;
    }

    @Override
    public ApiResponse<AccountInfoDTO> removeCartItem(long cartItemId) {
        Optional<CartItem> cartItem = cartItemRepository.findById(cartItemId);
        if (!cartItem.isPresent()) {
            return createErrorResponse(ErrorCode.NOT_FOUND, Message.CART_ITEM_NOT_FOUND);
        }
        cartItemRepository.delete(cartItem.get());
        Optional<Cart> cart = cartRepository.findById(cartItem.get().getCart().getCartId());
        if (cart.isPresent()) {
            List<CartItem> cartList = cartItemRepository.findAllByCart_CartId(cart.get().getCartId());
            cart.get().setTotalItem(cartList.size());
            cartRepository.save(cart.get());
        }
        return accountService.getCurrentUserInfo();
    }


}
