package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.Message;
import com.example.shoppingsystem.constants.Regex;
import com.example.shoppingsystem.dtos.OrderDTO;
import com.example.shoppingsystem.dtos.OrderDetailDTO;
import com.example.shoppingsystem.dtos.ProductVariantDTO;
import com.example.shoppingsystem.entities.*;
import com.example.shoppingsystem.enums.OrderStatus;
import com.example.shoppingsystem.repositories.*;
import com.example.shoppingsystem.requests.CheckoutRequest;
import com.example.shoppingsystem.requests.OrderDetailRequest;
import com.example.shoppingsystem.requests.OrderRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductServiceImpl productService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository, CartItemRepository cartItemRepository, CartRepository cartRepository, AccountRepository accountRepository, ProductRepository productRepository, ProductVariantRepository productVariantRepository, ProductServiceImpl productService) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.productService = productService;
    }

    @Override
    public ApiResponse<OrderDTO> createOrder(OrderRequest orderRequest) {
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isPresent()) {
            OrderList orderList = new OrderList();
            orderList.setOrderDate(LocalDateTime.now());
            orderList.setAddressDetail(orderRequest.getAddress_detail());
            System.out.println(Regex.parseVNDToBigDecimal(orderRequest.getTotal_bill()));
            orderList.setTotalPrice(Regex.parseVNDToBigDecimal(orderRequest.getTotal_bill()));
            orderList.setOrderStatus(OrderStatus.SHIPPING);
            orderList.setAccount(account.get());
            OrderList savedOrder = orderRepository.save(orderList);

            for (OrderDetailRequest orderDetailRequest : orderRequest.getOrder_items()) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrderList(savedOrder);

                Optional<Product> product = productRepository.findById(orderDetailRequest.getProductId());
                product.ifPresent(orderDetail::setProduct);
                Optional<ProductVariant> productVariant = productVariantRepository.findById(orderDetailRequest.getProductVariantId());
                productVariant.ifPresent(orderDetail::setProductVariant);

                orderDetail.setPrice(Regex.parseVNDToBigDecimal(orderDetailRequest.getPrice()));
                orderDetail.setQuantity(orderDetailRequest.getQuantity());
                orderDetail.setSubtotal(Regex.parseVNDToBigDecimal(orderDetailRequest.getSubtotal()));
                orderDetailRepository.save(orderDetail);

                Optional<Cart> cart = cartRepository.findByAccount_AccountId(account.get().getAccountId());
                if (cart.isPresent()) {
                    if (product.isPresent() && productVariant.isPresent()) {
                        Optional<CartItem> existCartItem = cartItemRepository.findByCartAndProductAndProductVariantAndQuantity(cart.get(), product.get(), productVariant.get(), orderDetailRequest.getQuantity());
                        existCartItem.ifPresent(cartItemRepository::delete);
                        cart.get().setTotalItem(cart.get().getTotalItem() - 1);
                        cartRepository.save(cart.get());
                    }

                }
            }
            return getOrderInformation(savedOrder.getOrderId());
        }
        return ApiResponse.<OrderDTO>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<List<OrderDetailDTO>> getOrderDetailCheckout(CheckoutRequest request) {
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isPresent()) {
            List<CartItem> cartItems = new ArrayList<>();
            for(long cartItemId : request.getCart_item_ids()) {
                Optional<CartItem> cartItem = cartItemRepository.findById(cartItemId);
                cartItem.ifPresent(cartItems::add);
            }

            List<OrderDetailDTO> orderLists = new ArrayList<>();
            for(CartItem cartItem : cartItems) {
                BigDecimal price = (cartItem.getProductVariant() != null) ? cartItem.getProductVariant().getSalePrice() : cartItem.getProduct().getSalePrice();
                BigDecimal quantityBigDecimal = new BigDecimal(cartItem.getQuantity());
                BigDecimal subTotal = price.multiply(quantityBigDecimal);
                OrderDetail orderDetail = new OrderDetail(0L,new OrderList(),cartItem.getProduct(), cartItem.getProductVariant(), cartItem.getQuantity(), price, subTotal);
                orderLists.add(convertOrderDetailToDTO(orderDetail));
            }


            return ApiResponse.<List<OrderDetailDTO>>builder()
                    .data(orderLists)
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<List<OrderDetailDTO>>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<List<OrderDTO>> cancelOrder(long orderId) {
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isPresent()) {
            Optional<OrderList> order = orderRepository.findById(orderId);
            if(order.isPresent()){
                order.get().setOrderStatus(OrderStatus.CANCELLED);
                orderRepository.save(order.get());
                return getAllOrder();
            } else {
                return ApiResponse.<List<OrderDTO>>builder()
                        .status(ErrorCode.BAD_REQUEST)
                        .message(Message.NOT_FOUND_ORDER)
                        .timestamp(new java.util.Date())
                        .build();
            }
        }
        return ApiResponse.<List<OrderDTO>>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<List<OrderDTO>> getAllOrder() {

        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isPresent()) {
            List<OrderList> orderLists = orderRepository.findAllByAccount_AccountId(account.get().getAccountId());
            orderLists.sort(Comparator.comparing(OrderList::getOrderDate).reversed());
            List<OrderDTO> orderDTOs = new ArrayList<>();
            for (OrderList orderList : orderLists) {
                List<OrderDetail> orderDetails = orderDetailRepository.findAllByOrderList_OrderId(orderList.getOrderId());
                OrderDTO orderDTO = convertOrderToDTO(orderList, orderDetails);
                orderDTOs.add(orderDTO);
            }

            return ApiResponse.<List<OrderDTO>>builder()
                    .data(orderDTOs)
                    .status(ErrorCode.SUCCESS)
                    .message("Successfully fetched orders")
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<List<OrderDTO>>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    public OrderDetailDTO convertOrderDetailToDTO(OrderDetail orderDetail) {
        ProductVariant productVariant = orderDetail.getProductVariant();
        ProductVariantDTO productVariantDTO = new ProductVariantDTO();
        productVariantDTO.setProduct_variant_id(productVariant.getProductVariantId());
        productVariantDTO.setProduct_variant_name(productVariant.getProductVariantName());
        productVariantDTO.setProduct_variant_image_url(productService.findImageProductVariant(productVariant));
        productVariantDTO.setOrigin_price(Regex.formatPriceToVND(productVariant.getListPrice()));
        productVariantDTO.setSale_price(Regex.formatPriceToVND(productVariant.getSalePrice()));
        productVariantDTO.setQuantity_in_stock(productVariant.getDesiredQuantity());
        return OrderDetailDTO.builder()
                .productInfoDTO(productService.getProductInformation(orderDetail.getProduct()))
                .productVariantDTO(productVariantDTO)
                .price(Regex.formatPriceToVND(orderDetail.getPrice()))
                .quantity(orderDetail.getQuantity())
                .sub_total(Regex.formatPriceToVND(orderDetail.getSubtotal()))
                .build();
    }

    public OrderDTO convertOrderToDTO(OrderList orderList, List<OrderDetail> orderDetails) {
        List<OrderDetailDTO> orderDetailDTOS = orderDetails.stream()
                .map(this::convertOrderDetailToDTO)
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .order_id(orderList.getOrderId())
                .address_info(Regex.parseShippingInfo(orderList.getAddressDetail()))
                .order_date(Regex.convertLocalDateTimeToDate(orderList.getOrderDate()))
                .order_status(String.valueOf(orderList.getOrderStatus()))
                .totalBill(Regex.formatPriceToVND(orderList.getTotalPrice()))
                .order_detail(orderDetailDTOS)
                .build();
    }

    @Override
    public ApiResponse<OrderDTO> getOrderInformation(Long orderId) {
        Optional<OrderList> optionalOrderList = orderRepository.findById(orderId);

        if (!optionalOrderList.isPresent()) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message("Order not found")
                    .timestamp(new java.util.Date())
                    .build();
        }

        OrderList orderList = optionalOrderList.get();
        List<OrderDetail> orderDetails = orderDetailRepository.findAllByOrderList_OrderId(orderList.getOrderId());
        OrderDTO orderDTO = convertOrderToDTO(orderList, orderDetails);

        return ApiResponse.<OrderDTO>builder()
                .data(orderDTO)
                .status(ErrorCode.SUCCESS)
                .message("Successfully fetched order information")
                .timestamp(new java.util.Date())
                .build();
    }

}
