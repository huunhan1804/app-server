package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.Message;
import com.example.shoppingsystem.constants.Regex;
import com.example.shoppingsystem.constants.StatusCode;
import com.example.shoppingsystem.dtos.*;
import com.example.shoppingsystem.entities.*;
import com.example.shoppingsystem.enums.MultimediaType;
import com.example.shoppingsystem.enums.OrderStatus;
import com.example.shoppingsystem.repositories.*;
import com.example.shoppingsystem.requests.*;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.responses.ShipmentResponse;
import com.example.shoppingsystem.services.interfaces.AccountService;
import com.example.shoppingsystem.services.interfaces.AgencyService;
import jakarta.persistence.criteria.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AgencyServiceImpl implements AgencyService {
    private final ProductRepository productRepository;
    private final ApprovalStatusRepository approvalStatusRepository;
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final MultimediaRepository multimediaRepository;
    private final FeedbackRepository feedbackRepository;
    private final AccountService accountService;
    private final ProductServiceImpl productService;
    private final AgencyInfoRepository agencyInfoRepository;

    @Autowired
    public AgencyServiceImpl(ProductRepository productRepository, ApprovalStatusRepository approvalStatusRepository, OrderRepository orderRepository, AccountRepository accountRepository, CategoryRepository categoryRepository, ProductVariantRepository productVariantRepository, MultimediaRepository multimediaRepository, FeedbackRepository feedbackRepository, AccountService accountService, ProductServiceImpl productService, AgencyInfoRepository agencyInfoRepository) {
        this.productRepository = productRepository;
        this.approvalStatusRepository = approvalStatusRepository;
        this.orderRepository = orderRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantRepository = productVariantRepository;
        this.multimediaRepository = multimediaRepository;
        this.feedbackRepository = feedbackRepository;
        this.accountService = accountService;
        this.productService = productService;
        this.agencyInfoRepository = agencyInfoRepository;
    }

    @Override
    public ApiResponse<ProductInfoDTO> createProduct(AddNewProductRequest request){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isPresent()){
            Product product = new Product();
            product.setProductName(request.getProduct_name());
            product.setProductDescription(request.getProduct_description());
            product.setDesiredQuantity(request.getQuantity_in_stock());
            product.setInventoryQuantity(request.getQuantity_in_stock());
            product.setCategory(categoryRepository.findByCategoryId(request.getCategory_id()));
            product.setCreatedBy(account.get().getUsername());
            product.setListPrice(Regex.parseVNDToBigDecimal(request.getProduct_list_price()));
            product.setSalePrice(Regex.parseVNDToBigDecimal(request.getProduct_sale_price()));
            product.setSoldAmount(0);
            product.setIsSale(false);
            product.setApprovalStatus(approvalStatusRepository.findApprovalStatusByStatusCode(StatusCode.STATUS_PENDING));
            Product saveProduct = productRepository.save(product);
            multimediaRepository.save(Multimedia.builder()
                    .product(product)
                    .account(account.get())
                    .multimediaType(MultimediaType.IMAGE)
                    .multimediaUrl(request.getImage_url())
                    .build()
            );
            for(AddProductVariantsRequest addProductVariantsRequest : request.getProduct_variant_list()){
                ProductVariant productVariant = new ProductVariant();
                productVariant.setProduct(product);
                productVariant.setListPrice(Regex.parseVNDToBigDecimal(addProductVariantsRequest.getOrigin_price()));
                productVariant.setSalePrice(Regex.parseVNDToBigDecimal(addProductVariantsRequest.getSale_price()));
                productVariant.setProductVariantName(addProductVariantsRequest.getProduct_variant_name());
                productVariant.setDesiredQuantity(addProductVariantsRequest.getQuantity_in_stock());
                productVariant.setInventoryQuantity(addProductVariantsRequest.getQuantity_in_stock());
                productVariantRepository.save(productVariant);
            }
            return getInfoProduct(saveProduct.getProductId());
        }
        return ApiResponse.<ProductInfoDTO>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new Date())
                .build();
    }

    @Override
    public ApiResponse<ProductInfoDTO> updateProduct(UpdateProductRequest request){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isPresent()){
            List<Product> products = new ArrayList<Product>();
            Optional<AgencyInfo> agency = agencyInfoRepository.findByAccount(account.get());
            if(agency.isPresent()){
                products = productRepository.findProductByAgencyInfoAndProductId(agency.get(), request.getProduct_id());
            }
            if(products.isEmpty()){
                return ApiResponse.<ProductInfoDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.PRODUCT_NOT_FOUND)
                        .timestamp(new Date())
                        .build();
            }else{
                Product product = products.get(0);
                product.setProductName(request.getProduct_name());
                product.setProductDescription(request.getProduct_description());
                product.setDesiredQuantity(request.getQuantity_in_stock());
                product.setInventoryQuantity(request.getQuantity_in_stock());
                product.setCategory(categoryRepository.findByCategoryId(request.getCategory_id()));
                product.setCreatedBy(account.get().getUsername());
                product.setListPrice(Regex.parseVNDToBigDecimal(request.getProduct_list_price()));
                product.setSalePrice(Regex.parseVNDToBigDecimal(request.getProduct_sale_price()));
                product.setIsSale(false);
                product.setApprovalStatus(approvalStatusRepository.findApprovalStatusByStatusCode(StatusCode.STATUS_PENDING));
                Product saveProduct = productRepository.save(product);
                for(AddProductVariantsRequest addProductVariantsRequest : request.getProduct_variant_list()){
                    ProductVariant productVariant = new ProductVariant();
                    productVariant.setProduct(product);
                    productVariant.setListPrice(Regex.parseVNDToBigDecimal(addProductVariantsRequest.getOrigin_price()));
                    productVariant.setSalePrice(Regex.parseVNDToBigDecimal(addProductVariantsRequest.getSale_price()));
                    productVariant.setProductVariantName(addProductVariantsRequest.getProduct_variant_name());
                    productVariant.setDesiredQuantity(addProductVariantsRequest.getQuantity_in_stock());
                    productVariant.setInventoryQuantity(addProductVariantsRequest.getQuantity_in_stock());
                    productVariantRepository.save(productVariant);
                }
                return getInfoProduct(saveProduct.getProductId());
            }
        }
        return ApiResponse.<ProductInfoDTO>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new Date())
                .build();
    }
    @Override
    public ApiResponse<AccountInfoDTO> deleteProduct(Long product_id){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isPresent()){
            List<Product> products;
            Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount(account.get());
            if (agencyInfo.isPresent()) {
                products = productRepository.findProductByAgencyInfoAndProductId(agencyInfo.get(), product_id);
                if (products.isEmpty()) {
                    return ApiResponse.<AccountInfoDTO>builder()
                            .status(ErrorCode.NOT_FOUND)
                            .message(Message.PRODUCT_NOT_FOUND)
                            .timestamp(new Date())
                            .build();
                }
                productRepository.deleteById(product_id);
                return accountService.getCurrentUserInfo();
            }
            return ApiResponse.<AccountInfoDTO>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.AGENCY_INFO_NOT_FOUND)
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<AccountInfoDTO>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new Date())
                .build();
    }

    @Override
    public ApiResponse<List<ProductInfoDTO>> getListProductByStatus(String status_code){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isPresent()){
            Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount(account.get());
            if(agencyInfo.isPresent()){
                List<Product> results = productRepository.findByAgencyInfo(agencyInfo.get());
                results.sort(Comparator.comparing(Product::getProductName));
                List<ProductInfoDTO> productDTOs = new ArrayList<>();
                for(Product product : results){
                    if(product.getApprovalStatus().getStatusCode().equals(status_code)){
                        List<ProductVariant> productVariants = product.getProductVariants();
                        List<Feedback> feedbacks = feedbackRepository.findByProduct_ProductId(product.getProductId());
                        List<Multimedia> multimedias = multimediaRepository.findAllByProduct_ProductId(product.getProductId());
                        ProductInfoDTO productInfoDTO = convertToProductInfoDTO(product, productVariants, feedbacks, multimedias);
                        productDTOs.add(productInfoDTO);
                    }
                }
                return ApiResponse.<List<ProductInfoDTO>>builder()
                        .data(productDTOs)
                        .status(ErrorCode.SUCCESS)
                        .message("Successfully fetched products")
                        .timestamp(new java.util.Date())
                        .build();
            }

            return ApiResponse.<List<ProductInfoDTO>>builder()
                    .status(ErrorCode.FORBIDDEN)
                    .message(Message.AGENCY_INFO_NOT_FOUND)
                    .timestamp(new java.util.Date())
                    .build();

        }
        return ApiResponse.<List<ProductInfoDTO>>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    public ApiResponse<ProductInfoDTO> getInfoProduct(long productId) {
        Optional<Product> product = productRepository.findById(productId);
        if(product.isPresent()){
            return ApiResponse.<ProductInfoDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(getProductInformation(product.get()))
                    .timestamp(new java.util.Date())
                    .build();
        } else {
            return ApiResponse.<ProductInfoDTO>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.NOT_FOUND)
                    .timestamp(new java.util.Date())
                    .build();
        }
    }

    public ProductInfoDTO getProductInformation(Product product){
        List<ProductVariant> productVariants = productVariantRepository.findAllByProduct_ProductId(product.getProductId());
        List<Feedback> feedbacks = feedbackRepository.findByProduct_ProductId(product.getProductId());
        List<Multimedia> multimediaProduct = multimediaRepository.  findAllByProduct_ProductId(product.getProductId());
        return convertToProductInfoDTO(product, productVariants, feedbacks, multimediaProduct);
    }

    private ProductInfoDTO convertToProductInfoDTO(Product product, List<ProductVariant> productVariants, List<Feedback> feedbacks, List<Multimedia> multimediaProduct) {
        ProductInfoDTO productInfoDTO = new ProductInfoDTO();
        productInfoDTO.setProduct_id(product.getProductId());
        productInfoDTO.setProduct_name(product.getProductName());
        productInfoDTO.setProduct_price(Regex.formatPriceToVND(product.getSalePrice()));
        productInfoDTO.setRating(calculateAverageRating(product));
        productInfoDTO.setProduct_description(product.getProductDescription());
        productInfoDTO.setQuantity_in_stock(product.getDesiredQuantity());

        List<String> mediaUrls = multimediaProduct.stream()
                .map(Multimedia::getMultimediaUrl)
                .collect(Collectors.toList());

        productInfoDTO.setMedia_url(mediaUrls);

        List<ProductVariantDTO> productVariantDTOs = productVariants.stream()
                .map(productVariant -> {
                    ProductVariantDTO productVariantDTO = new ProductVariantDTO();
                    productVariantDTO.setProduct_variant_id(productVariant.getProductVariantId());
                    productVariantDTO.setProduct_variant_name(productVariant.getProductVariantName());
                    productVariantDTO.setProduct_variant_image_url(findImageProductVariant(productVariant));
                    productVariantDTO.setOrigin_price(Regex.formatPriceToVND(productVariant.getListPrice()));
                    productVariantDTO.setSale_price(Regex.formatPriceToVND(productVariant.getSalePrice()));
                    productVariantDTO.setQuantity_in_stock(productVariant.getDesiredQuantity());
                    return productVariantDTO;
                })
                .collect(Collectors.toList());

        productInfoDTO.setProduct_variant_list(productVariantDTOs);

        List<FeedbackDTO> feedbackDTOs = feedbacks.stream()
                .map(feedback -> {
                    FeedbackDTO feedbackDTO = new FeedbackDTO();
                    feedbackDTO.setUser_name(feedback.getAccount().getFullname());
                    feedbackDTO.setRating(feedback.getRating().getValue());
                    feedbackDTO.setComment(feedback.getComment());
                    return feedbackDTO;
                })
                .collect(Collectors.toList());

        productInfoDTO.setFeedback_list(feedbackDTOs);

        return productInfoDTO;
    }
    private double calculateAverageRating(Product product) {
        List<Feedback> feedbacks = feedbackRepository.findByProduct_ProductId(product.getProductId());
        if (feedbacks.isEmpty()) {
            return 0.0;
        }

        int sumRatings = feedbacks.stream()
                .mapToInt(feedback -> feedback.getRating().getValue())
                .sum();

        return (double) sumRatings / feedbacks.size();
    }

    public String findImageProduct(Product product) {
        Optional<Multimedia> multimedia = multimediaRepository.findByProduct_ProductId(product.getProductId());
        return multimedia.map(Multimedia::getMultimediaUrl).orElse(null);
    }

    public String findImageProductVariant(ProductVariant productVariant) {
        Optional<Multimedia> multimedia = multimediaRepository.findAllByProductVariant_ProductVariantId(productVariant.getProductVariantId());
        return multimedia.map(Multimedia::getMultimediaUrl).orElse(null);
    }

    private ApiResponse<AccountInfoDTO> createErrorResponse(int errorCode, String message) {
        return ApiResponse.<AccountInfoDTO>builder()
                .status(errorCode)
                .message(message)
                .timestamp(new Date())
                .build();
    }

    @Override
    public ApiResponse<OrderDTO> confirmOrder(ConfirmOrderRequest request){
        Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByApplicationId(request.getAgencyId());
        if(agencyInfo.isPresent()) {
            OrderList orderList = orderRepository.findByOrderId(request.getOrderId());

            if(orderList == null) {
                return ApiResponse.<OrderDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.NOT_FOUND_ORDER)
                        .timestamp(new java.util.Date())
                        .build();
            }
            if(!orderList.getOrderStatus().equals(OrderStatus.PENDING)) {
                return ApiResponse.<OrderDTO>builder()
                        .status(ErrorCode.BAD_REQUEST)
                        .message(Message.ORDER_IS_NOT_PENDING)
                        .timestamp(new java.util.Date())
                        .build();
            }
            //Xac minh order la cua agency

            orderList.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(orderList);

            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.CONFIRM_ORDER_SUCCESS)
                    .data(convertOrderToDTO(orderList, (List<OrderDetail>) orderList.getOrderDetails()))
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<OrderDTO>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.AGENCY_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<String> getOrderStatus(Long order_id){
        OrderList orderList = orderRepository.findByOrderId(order_id);
        if(orderList == null) {
            String orderStatus = orderList.getOrderStatus().name();
            return ApiResponse.<String>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(orderStatus)
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<String>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.NOT_FOUND_ORDER)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<List<OrderDTO>> getListOfOrdersByStatus(ListOrderByStatusRequest request){
        Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByApplicationId(request.getAgencyId());
        if(agencyInfo.isPresent()) {
            Account account = agencyInfo.get().getAccount();
            List<OrderList> orderLists = orderRepository.findAllByAccount_AccountId(account.getAccountId());
            if(orderLists.isEmpty()) {
                return ApiResponse.<List<OrderDTO>>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.NOT_FOUND_ORDER)
                        .timestamp(new java.util.Date())
                        .build();
            }
            List<OrderDTO> orderDTOS = new ArrayList<>();
            for(OrderList orderList : orderLists) {
                if(orderList.getOrderStatus().equals(OrderStatus.valueOf(request.getStatus()))) {
                    OrderDTO orderDTO = convertOrderToDTO(orderList, (List<OrderDetail>) orderList.getOrderDetails());
                    orderDTOS.add(orderDTO);
                }
            }
            return ApiResponse.<List<OrderDTO>>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.FETCHING_ORDER_SUCCESS)
                    .data(orderDTOS)
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<List<OrderDTO>>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.AGENCY_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ShipmentResponse shipOrder(Long order_id, String agency_email) throws AccessDeniedException {
//        OrderList orderList = orderRepository.findByOrderId(order_id);
//        if(orderList != null) {
//            boolean ownsProduct = orderList.getOrderDetails().stream().anyMatch(item -> item.getProduct().getAccount().getEmail().equals(agency_email));
//            if(!ownsProduct) {
//                throw new AccessDeniedException(String.format(Message.AGENCY_NOT_ALLOWED));
//            }
//            ShippingRequest shippingRequest;
//        }
        return null;
    }

//    private ShippingRequest createShippingRequest(OrderList orderList){
//        List<OrderDetail> orderDetails = (List<OrderDetail>) orderList.getOrderDetails();
//        OrderDTO orderDTO = convertOrderToDTO(orderList, orderDetails);
//        List<ShippingItem> items = orderList.getOrderDetails().stream().map(item-> ShippingItem.builder()
//                .name(item.getProduct().getProductName())
//                .quantity(item.getQuantity())
//                .price(item.getPrice())
//                .build()).collect(Collectors.toList()).reversed();
//
//        return null;
//    }

    public ShippingRequest convertToShippingRequest(OrderDTO orderDTO) {
        AddressInfoDTO address = orderDTO.getAddress_info();

        List<ShippingItem> items = orderDTO.getOrder_detail().stream().map(detail -> {
            ShippingItem item = new ShippingItem();
            item.setName(detail.getProductInfoDTO().getProduct_name());
            item.setQuantity(detail.getQuantity());
            item.setPrice(new BigDecimal(detail.getPrice()));
            return item;
        }).collect(Collectors.toList());

        return ShippingRequest.builder()
                .order_code("ORDER-" + orderDTO.getOrder_id())
                .receiver_name(address.getFullname())
                .receiver_phone(address.getPhone())
                .receiver_address(address.getAddress_detail())
                .total_amount(new BigDecimal(orderDTO.getTotalBill()))
                .items(items)
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
}
