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
import com.example.shoppingsystem.requests.ReturnOrderRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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
    private final AgencyInfoRepository agencyInfoRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository, CartItemRepository cartItemRepository, CartRepository cartRepository, AccountRepository accountRepository, ProductRepository productRepository, ProductVariantRepository productVariantRepository, ProductServiceImpl productService, AgencyInfoRepository agencyInfoRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.productService = productService;
        this.agencyInfoRepository = agencyInfoRepository;
    }

//    @Override
//    public ApiResponse<OrderDTO> createOrder(OrderRequest orderRequest) {
//        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
//        if (account.isPresent()) {
//            OrderList orderList = new OrderList();
//            orderList.setOrderDate(LocalDateTime.now());
//            orderList.setAddressDetail(orderRequest.getAddress_detail());
//            System.out.println(Regex.parseVNDToBigDecimal(orderRequest.getTotal_bill()));
//            orderList.setTotalPrice(Regex.parseVNDToBigDecimal(orderRequest.getTotal_bill()));
//            orderList.setOrderStatus(OrderStatus.SHIPPED);
//            orderList.setAccount(account.get());
//            OrderList savedOrder = orderRepository.save(orderList);
//
//            for (OrderDetailRequest orderDetailRequest : orderRequest.getOrder_items()) {
//                OrderDetail orderDetail = new OrderDetail();
//                orderDetail.setOrderList(savedOrder);
//
//                Optional<Product> product = productRepository.findById(orderDetailRequest.getProductId());
//                product.ifPresent(orderDetail::setProduct);
//                Optional<ProductVariant> productVariant = productVariantRepository.findById(orderDetailRequest.getProductVariantId());
//                productVariant.ifPresent(orderDetail::setProductVariant);
//
//                orderDetail.setPrice(Regex.parseVNDToBigDecimal(orderDetailRequest.getPrice()));
//                orderDetail.setQuantity(orderDetailRequest.getQuantity());
//                orderDetail.setSubtotal(Regex.parseVNDToBigDecimal(orderDetailRequest.getSubtotal()));
//                orderDetailRepository.save(orderDetail);
//
//                Optional<Cart> cart = cartRepository.findByAccount_AccountId(account.get().getAccountId());
//                if (cart.isPresent()) {
//                    if (product.isPresent() && productVariant.isPresent()) {
//                        Optional<CartItem> existCartItem = cartItemRepository.findByCartAndProductAndProductVariantAndQuantity(cart.get(), product.get(), productVariant.get(), orderDetailRequest.getQuantity());
//                        existCartItem.ifPresent(cartItemRepository::delete);
//                        cart.get().setTotalItem(cart.get().getTotalItem() - 1);
//                        cartRepository.save(cart.get());
//                    }
//
//                }
//            }
//            return getOrderInformation(savedOrder.getOrderId());
//        }
//        return ApiResponse.<OrderDTO>builder()
//                .status(ErrorCode.FORBIDDEN)
//                .message(Message.ACCOUNT_NOT_FOUND)
//                .timestamp(new java.util.Date())
//                .build();
//    }

    @Transactional
    @Override
    public ApiResponse<OrderDTO> createOrder(OrderRequest orderRequest) {
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isPresent()) {
            OrderList orderList = new OrderList();
            orderList.setOrderDate(LocalDateTime.now());
            orderList.setAddressDetail(orderRequest.getAddress_detail());
            System.out.println(Regex.parseVNDToBigDecimal(orderRequest.getTotal_bill()));
            orderList.setTotalPrice(Regex.parseVNDToBigDecimal(orderRequest.getTotal_bill()));
            orderList.setOrderStatus(OrderStatus.PENDING);
            orderList.setAccount(account.get());

            Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByApplicationId(productRepository.findByProductId(orderRequest.getOrder_items().get(0).getProductId()).getAgencyInfo().getApplicationId());
            assert agencyInfo.orElse(null) != null;
            orderList.setAgency(agencyInfo.orElse(null));
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

    @Transactional
    @Override
    public ApiResponse<List<OrderDTO>> createOrders(OrderRequest orderRequest){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isEmpty()) {
            return ApiResponse.<List<OrderDTO>>builder()
                    .status(ErrorCode.FORBIDDEN)
                    .message(Message.ACCOUNT_NOT_FOUND)
                    .timestamp(new java.util.Date())
                    .build();
        }

        Account customerAccount = account.get();

        Map<Long, List<OrderDetailRequest>> groupedByAgency = new HashMap<>();
        for (OrderDetailRequest item : orderRequest.getOrder_items()) {
            Optional<Product> product = productRepository.findById(item.getProductId());
            if (product.isPresent()) {
                Long agencyId = product.get().getAgencyInfo().getApplicationId();
                groupedByAgency.computeIfAbsent(agencyId, k -> new ArrayList<>()).add(item);
            }
        }

        List<OrderDTO> orderDTOs = new ArrayList<>();
        for (Map.Entry<Long, List<OrderDetailRequest>> entry : groupedByAgency.entrySet()) {
            Long agencyId = entry.getKey();
            List<OrderDetailRequest> items = entry.getValue();

            Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findById(agencyId);
            if (agencyInfo.isEmpty()) {continue;}

            OrderList orderList = new OrderList();
            orderList.setOrderDate(LocalDateTime.now());
            orderList.setAddressDetail(orderRequest.getAddress_detail());
            orderList.setOrderStatus(OrderStatus.PENDING);
            orderList.setAccount(customerAccount);
            orderList.setAgency(agencyInfo.orElse(null));

            BigDecimal totalPrice = items.stream()
                    .map(i -> Regex.parseVNDToBigDecimal(i.getSubtotal()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            orderList.setTotalPrice(totalPrice);

            OrderList savedOrder = orderRepository.save(orderList);

            for (OrderDetailRequest item : items) {
                Optional<Product> productOpt = productRepository.findById(item.getProductId());
                Optional<ProductVariant> variantOpt = productVariantRepository.findById(item.getProductVariantId());

                if (productOpt.isEmpty()) continue;

                Product productEntity = productOpt.get();
                ProductVariant variantEntity = variantOpt.orElse(null);
                if (variantEntity == null) {
                    throw new RuntimeException("Product variant: '" + Objects.requireNonNull(productVariantRepository.findById(item.getProductVariantId()).orElse(null)).getProductVariantName() + "' not found");
                }

                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrderList(savedOrder);
                orderDetail.setProduct(productEntity);
                orderDetail.setProductVariant(variantEntity);
                orderDetail.setPrice(Regex.parseVNDToBigDecimal(item.getPrice()));
                orderDetail.setQuantity(item.getQuantity());
                orderDetail.setSubtotal(Regex.parseVNDToBigDecimal(item.getSubtotal()));
                orderDetailRepository.save(orderDetail);

                int newVariantInventory = variantEntity.getInventoryQuantity() - item.getQuantity();
                variantEntity.setInventoryQuantity(newVariantInventory);
                productVariantRepository.save(variantEntity);

                int newProductInventory = productEntity.getInventoryQuantity() - item.getQuantity();
                productEntity.setInventoryQuantity(newProductInventory);
                int currentSold = productEntity.getSoldAmount() != null ? productEntity.getSoldAmount() : 0;
                productEntity.setSoldAmount(currentSold + item.getQuantity());
                productRepository.save(productEntity);
            }
            orderDTOs.add(getOrderInformation(savedOrder.getOrderId()).getData());
        }
        return ApiResponse.<List<OrderDTO>>builder()
                .status(ErrorCode.SUCCESS)
                .message(Message.SUCCESS)
                .data(orderDTOs)
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
    public ApiResponse<List<OrderDTO>> receiveOrder(long orderId){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isPresent()) {
            Optional<OrderList> order = orderRepository.findById(orderId);
            if(order.isPresent()){
                order.get().setOrderStatus(OrderStatus.DELIVERED);
                orderRepository.save(order.get());
                return getAllOrder();
            }else {
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
    public ApiResponse<List<OrderDTO>> returnOrder(ReturnOrderRequest request) {
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isPresent()) {
            Optional<OrderList> order = orderRepository.findById(request.getOrderId());
            if(order.isPresent()){
                if(order.get().getUpdatedDate().isBefore(LocalDateTime.now().minusDays(7))){
                    return ApiResponse.<List<OrderDTO>>builder()
                            .status(ErrorCode.BAD_REQUEST)
                            .message("Đơn hàng đã qua 7 ngày.")
                            .timestamp(new java.util.Date())
                            .build();
                }
                order.get().setOrderStatus(OrderStatus.RETURNED);
                order.get().setReturnReason(request.getReturnReason());
                orderRepository.save(order.get());
                return getAllOrder();
            }else {
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
                .returnReason(orderList.getReturnReason())
                .build();
    }

    @Override
    public ApiResponse<OrderDTO> getOrderInformation(Long orderId) {
        Optional<OrderList> optionalOrderList = orderRepository.findById(orderId);

        if (optionalOrderList.isEmpty()) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.NOT_FOUND_ORDER)
                    .timestamp(new java.util.Date())
                    .build();
        }

        OrderList orderList = optionalOrderList.get();
        List<OrderDetail> orderDetails = orderDetailRepository.findAllByOrderList_OrderId(orderList.getOrderId());
        OrderDTO orderDTO = convertOrderToDTO(orderList, orderDetails);

        return ApiResponse.<OrderDTO>builder()
                .data(orderDTO)
                .status(ErrorCode.SUCCESS)
                .message(Message.FETCHING_ORDER_SUCCESS)
                .timestamp(new java.util.Date())
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<OrderDTO> reorder(long orderId){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isEmpty()) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.FORBIDDEN)
                    .message(Message.ACCOUNT_NOT_FOUND)
                    .timestamp(new java.util.Date())
                    .build();
        }

        Optional<OrderList> oldOrderOptional = orderRepository.findById(orderId);
        if (oldOrderOptional.isEmpty()) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.NOT_FOUND_ORDER)
                    .timestamp(new java.util.Date())
                    .build();
        }
        OrderList oldOrder = oldOrderOptional.get();

        if (!oldOrder.getAccount().getAccountId().equals(account.get().getAccountId())) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.FORBIDDEN)
                    .message("You did not purchase this order.")
                    .timestamp(new java.util.Date())
                    .build();
        }

        OrderList newOrder = new OrderList();
        newOrder.setAccount(oldOrder.getAccount());
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setAddressDetail(oldOrder.getAddressDetail());
        newOrder.setTotalPrice(oldOrder.getTotalPrice());
        newOrder.setOrderStatus(OrderStatus.PENDING);
        newOrder.setAgency(oldOrder.getAgency());

        OrderList savedNewOrder = orderRepository.save(newOrder);

        List<OrderDetail> newDetails = new ArrayList<>();
        List<OrderDetail> oldDetails = orderDetailRepository.findAllByOrderList_OrderId(oldOrder.getOrderId());
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderDetail oldDetail : oldDetails) {
            Product product = oldDetail.getProduct();
            ProductVariant productVariant = oldDetail.getProductVariant();
            int quantity = oldDetail.getQuantity();

            int inventoryQuantity = productVariant != null ? productVariant.getInventoryQuantity() : product.getInventoryQuantity();

            if (quantity > inventoryQuantity) {
                return ApiResponse.<OrderDTO>builder()
                        .status(ErrorCode.BAD_REQUEST)
                        .message("Product'" + product.getProductName() + "' is not enough inventory.")
                        .timestamp(new Date())
                        .build();

            }

            OrderDetail newDetail = new OrderDetail();
            newDetail.setOrderList(savedNewOrder);
            newDetail.setProduct(oldDetail.getProduct());
            newDetail.setProductVariant(oldDetail.getProductVariant());
            newDetail.setQuantity(oldDetail.getQuantity());
            newDetail.setPrice(oldDetail.getPrice());
            newDetail.setSubtotal(oldDetail.getSubtotal());
            newDetails.add(newDetail);

            if (productVariant != null) {
                productVariant.setInventoryQuantity(productVariant.getInventoryQuantity() - quantity);
                productVariantRepository.save(productVariant);
            }
            product.setInventoryQuantity(product.getInventoryQuantity() - quantity);
            int currentSold = product.getSoldAmount() != null ? product.getSoldAmount() : 0;
            product.setSoldAmount(currentSold + quantity);
            productRepository.save(product);
            totalPrice = totalPrice.add(newDetail.getSubtotal());
        }

        orderDetailRepository.saveAll(newDetails);
        savedNewOrder.setTotalPrice(totalPrice);
        orderRepository.save(savedNewOrder);

        return getOrderInformation(savedNewOrder.getOrderId());
    }

}
