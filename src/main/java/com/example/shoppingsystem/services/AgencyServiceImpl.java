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
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.persistence.criteria.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
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
    public ApiResponse<ProductFullDTO> createProduct(AddNewProductRequest request){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isPresent()){
            AgencyInfo agencyInfo = agencyInfoRepository.findByAccount_AccountId(account.get().getAccountId()).get();
            Product product = new Product();
            product.setAgencyInfo(agencyInfo);
            product.setProductName(request.getProduct_name());
            product.setProductDescription(request.getProduct_description());
            product.setDesiredQuantity(request.getQuantity_in_stock());
            product.setInventoryQuantity(request.getQuantity_in_stock());
            product.setCategory(categoryRepository.findByCategoryId(request.getCategory_id()));
            product.setCreatedBy(agencyInfo.getFullNameApplicant());
            product.setCreatedDate(LocalDateTime.now());
            product.setListPrice(Regex.parseVNDToBigDecimal(request.getProduct_list_price()));
            product.setSalePrice(Regex.parseVNDToBigDecimal(request.getProduct_sale_price()));
            product.setSoldAmount(0);
            product.setIsSale(false);
            product.setApprovalStatus(approvalStatusRepository.findApprovalStatusByStatusCode(StatusCode.STATUS_PENDING));
            Product saveProduct = productRepository.save(product);
            for(String image_url : request.getImage_urls()){
                multimediaRepository.save(Multimedia.builder()
                        .product(product)
                        .multimediaType(MultimediaType.IMAGE)
                        .multimediaUrl(image_url)
                        .build()
                );
            }
            for(AddProductVariantsRequest addProductVariantsRequest : request.getProduct_variant_list()){
                ProductVariant productVariant = new ProductVariant();
                productVariant.setProduct(product);
                productVariant.setListPrice(Regex.parseVNDToBigDecimal(addProductVariantsRequest.getProduct_list_price()));
                productVariant.setSalePrice(Regex.parseVNDToBigDecimal(addProductVariantsRequest.getProduct_sale_price()));
                productVariant.setProductVariantName(addProductVariantsRequest.getProduct_variant_name());
                productVariant.setDesiredQuantity(addProductVariantsRequest.getInventory_quantity());
                productVariant.setInventoryQuantity(addProductVariantsRequest.getInventory_quantity());
                productVariantRepository.save(productVariant);
            }
            return getFullInfoProduct(saveProduct.getProductId());
        }
        return ApiResponse.<ProductFullDTO>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new Date())
                .build();
    }

    @Override
    public ApiResponse<ProductFullDTO> updateProduct(UpdateProductRequest request){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isPresent()){
            List<Product> products = new ArrayList<Product>();
            Optional<AgencyInfo> agency = agencyInfoRepository.findByAccount(account.get());
            if(agency.isPresent()){
                products = productRepository.findProductByAgencyInfoAndProductId(agency.get(), request.getProduct_id());
                if(products.isEmpty()){
                    return ApiResponse.<ProductFullDTO>builder()
                            .status(ErrorCode.NOT_FOUND)
                            .message(Message.PRODUCT_NOT_FOUND)
                            .timestamp(new Date())
                            .build();
                }else{
                    Product product = products.get(0);
                    product.setProductName(request.getProduct_name());
                    product.setProductDescription(request.getProduct_description());
                    //product.setDesiredQuantity(request.getQuantity_in_stock());
                    product.setInventoryQuantity(request.getQuantity_in_stock());
                    product.setCategory(categoryRepository.findByCategoryId(request.getCategory_id()));
                    product.setCreatedBy(account.get().getUsername());
                    product.setListPrice(Regex.parseVNDToBigDecimal(request.getProduct_list_price()));
                    product.setSalePrice(Regex.parseVNDToBigDecimal(request.getProduct_sale_price()));
                    product.setIsSale(false);
                    product.setApprovalStatus(approvalStatusRepository.findApprovalStatusByStatusCode(StatusCode.STATUS_PENDING));
                    Product saveProduct = productRepository.save(product);

                    multimediaRepository.deleteAllByProduct_ProductId(product.getProductId());
                    for(String image_url : request.getImage_urls()){
                        multimediaRepository.save(Multimedia.builder()
                                .product(product)
                                .multimediaType(MultimediaType.IMAGE)
                                .multimediaUrl(image_url)
                                .build()
                        );
                    }

                    for(AddProductVariantsRequest addProductVariantsRequest : request.getProduct_variant_list()){
                        ProductVariant productVariant = new ProductVariant();
                        productVariant.setProduct(product);
                        productVariant.setListPrice(Regex.parseVNDToBigDecimal(addProductVariantsRequest.getProduct_list_price()));
                        productVariant.setSalePrice(Regex.parseVNDToBigDecimal(addProductVariantsRequest.getProduct_sale_price()));
                        productVariant.setProductVariantName(addProductVariantsRequest.getProduct_variant_name());
                        productVariant.setDesiredQuantity(addProductVariantsRequest.getInventory_quantity());
                        productVariant.setInventoryQuantity(addProductVariantsRequest.getInventory_quantity());
                        productVariantRepository.save(productVariant);
                    }
                    return getFullInfoProduct(saveProduct.getProductId());
                }
            }
            return ApiResponse.<ProductFullDTO>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.AGENCY_NOT_FOUND)
                    .timestamp(new Date())
                    .build();
        }
        return ApiResponse.<ProductFullDTO>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new Date())
                .build();
    }
    @Override
    public ApiResponse<AgencyInfoDTO> deleteProduct(Long product_id){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isPresent()){
            List<Product> products;
            Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount(account.get());
            if (agencyInfo.isPresent()) {
                products = productRepository.findProductByAgencyInfoAndProductId(agencyInfo.get(), product_id);
                if (products.isEmpty()) {
                    return ApiResponse.<AgencyInfoDTO>builder()
                            .status(ErrorCode.NOT_FOUND)
                            .message(Message.PRODUCT_NOT_FOUND)
                            .timestamp(new Date())
                            .build();
                }
                productRepository.deleteById(product_id);
                return ApiResponse.<AgencyInfoDTO>builder()
                        .status(ErrorCode.SUCCESS)
                        .data(convertToAgencyInfoDTO(agencyInfo.get()))
                        .message(Message.DELETE_PRODUCT_SUCCESS)
                        .timestamp(new Date())
                        .build();
            }
            return ApiResponse.<AgencyInfoDTO>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.AGENCY_INFO_NOT_FOUND)
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<AgencyInfoDTO>builder()
                .status(ErrorCode.NOT_FOUND)
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
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.AGENCY_INFO_NOT_FOUND)
                    .timestamp(new java.util.Date())
                    .build();

        }
        return ApiResponse.<List<ProductInfoDTO>>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<ProductInfoDTO> disableSellingProduct(Long product_id){
        Product product = productRepository.findByProductId(product_id);
        if(product != null){
            if(product.getIsSale()){
                product.setIsSale(false);
                Product savedProduct = productRepository.save(product);
                ProductInfoDTO productInfoDTO = convertToProductInfoDTO(savedProduct, product.getProductVariants(), null, multimediaRepository.findAllByProduct_ProductId(product.getProductId()));
                return ApiResponse.<ProductInfoDTO>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.DISABLED_PRODUCT_SUCCESS)
                        .data(productInfoDTO)
                        .timestamp(new java.util.Date())
                        .build();
            }
            return ApiResponse.<ProductInfoDTO>builder()
                    .status(ErrorCode.CONFLICT)
                    .message(Message.PRODUCT_IS_NOT_SELLING)
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<ProductInfoDTO>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.NOT_FOUND_PRODUCT)
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

    public ApiResponse<ProductFullDTO> getFullInfoProduct(long productId) {
        Optional<Product> product = productRepository.findById(productId);
        if(product.isPresent()){
            return ApiResponse.<ProductFullDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(convertToProductFullDTO(product.get()))
                    .timestamp(new java.util.Date())
                    .build();
        } else {
            return ApiResponse.<ProductFullDTO>builder()
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
    public ProductFullDTO getFullProductInformation(Product product){
        List<ProductVariant> productVariants = productVariantRepository.findAllByProduct_ProductId(product.getProductId());
        List<Feedback> feedbacks = feedbackRepository.findByProduct_ProductId(product.getProductId());
        List<Multimedia> multimediaProduct = multimediaRepository.  findAllByProduct_ProductId(product.getProductId());
        return convertToProductFullDTO(product);
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

    private ProductFullDTO convertToProductFullDTO(Product product) {
        ProductFullDTO productFullDTO = new ProductFullDTO();
        productFullDTO.setProduct_id(product.getProductId());
        productFullDTO.setProduct_name(product.getProductName());
        productFullDTO.setProduct_description(product.getProductDescription());
        productFullDTO.setRating(calculateAverageRating(product));
        productFullDTO.setQuantity_in_stock(product.getDesiredQuantity());
        productFullDTO.setApproval_status(ApprovalStatusDTO.builder()
                .statusId(product.getApprovalStatus().getStatusId())
                .statusCode(product.getApprovalStatus().getStatusCode())
                .statusName(product.getApprovalStatus().getStatusName())
                .build());
        productFullDTO.setCategory(CategoryDTO.builder()
                .categoryId(product.getCategory().getCategoryId())
                .categoryName(product.getCategory().getCategoryName())
                .categoryDescription(product.getCategory().getCategoryDescription())
                .build());
        productFullDTO.setSold_amount(product.getSoldAmount());
        productFullDTO.setProduct_list_price(Regex.formatPriceToVND(product.getListPrice()));
        productFullDTO.setProduct_sale_price(Regex.formatPriceToVND(product.getSalePrice()));

        List<String> mediaUrls = multimediaRepository.findAllByProduct_ProductId(product.getProductId()).stream()
                .map(Multimedia::getMultimediaUrl)
                .toList();

        productFullDTO.setMedia_url(mediaUrls);

        List<ProductVariantDTO> productVariantDTOS = productVariantRepository.findAllByProduct_ProductId(product.getProductId()).stream()
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
                .toList();

        productFullDTO.setProduct_variant_list(productVariantDTOS);

        List<FeedbackDTO> feedbackDTOs = feedbackRepository.findByProduct_ProductId(product.getProductId()).stream()
                .map(feedback -> {
                    FeedbackDTO feedbackDTO = new FeedbackDTO();
                    feedbackDTO.setUser_name(feedback.getAccount().getFullname());
                    feedbackDTO.setRating(feedback.getRating().getValue());
                    feedbackDTO.setComment(feedback.getComment());
                    return feedbackDTO;
                })
                .toList();

        productFullDTO.setFeedback_list(feedbackDTOs);

        return productFullDTO;
    }

    private AgencyInfoDTO convertToAgencyInfoDTO(AgencyInfo agencyInfo){
        AgencyInfoDTO agencyInfoDTO = new AgencyInfoDTO();
        agencyInfoDTO.setAccount_id(agencyInfo.getAccount().getAccountId());
        agencyInfoDTO.setAgency_id(agencyInfo.getApplicationId());
        agencyInfoDTO.setAgency_name(agencyInfo.getShopName());
        agencyInfoDTO.setAgency_email(agencyInfo.getShopEmail());
        agencyInfoDTO.setAgency_phone(agencyInfo.getShopPhone());
        agencyInfoDTO.setAgency_address(agencyInfo.getShopAddressDetail());
        agencyInfoDTO.setAgency_tax_code(agencyInfo.getTaxNumber());

        agencyInfoDTO.setFull_name_applicant(agencyInfo.getFullNameApplicant());
        agencyInfoDTO.setId_card_number_applicant(agencyInfo.getIdCardNumber());
        agencyInfoDTO.setStatus(agencyInfo.getApprovalStatus().getStatusCode());
        agencyInfoDTO.setRejectionReason(agencyInfo.getRejectionReason());

        agencyInfoDTO.setBirth_date_applicant(agencyInfo.getBirthdateApplicant());
        agencyInfoDTO.setGender_applicant(agencyInfo.getGenderApplicant());
        agencyInfoDTO.setDate_of_issue_card(agencyInfo.getDateOfIssueIdCard());
        agencyInfoDTO.setPlace_of_issue_card(agencyInfo.getPlaceOfIssueIdCard());
        agencyInfoDTO.setId_card_front_image_url(agencyInfo.getIdCardFrontImageUrl());
        agencyInfoDTO.setId_card_back_image_url(agencyInfo.getIdCardBackImageUrl());

        agencyInfoDTO.setBusiness_license_urls(agencyInfo.getBusinessLicenseUrls());
        agencyInfoDTO.setProfessional_cert_urls(agencyInfo.getProfessionalCertUrls());
        agencyInfoDTO.setDiploma_cert_urls(agencyInfo.getDiplomaCertUrls());

        return agencyInfoDTO;
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
    public ApiResponse<OrderDTO> shipOrder(ShippingRequest request){
//        OrderList orderList = orderRepository.findByOrderId(order_id);
//        if(orderList != null) {
//            boolean ownsProduct = orderList.getOrderDetails().stream().anyMatch(item -> item.getProduct().getAccount().getEmail().equals(agency_email));
//            if(!ownsProduct) {
//                throw new AccessDeniedException(String.format(Message.AGENCY_NOT_ALLOWED));
//            }
//            ShippingRequest shippingRequest;
//        }
        OrderList orderList = orderRepository.findByOrderId(request.getOrderId());
        if(orderList != null) {
            if(agencyInfoRepository.findByAccount_AccountId(orderList.getAgency().getAccountId()).get().getApplicationId().equals(request.getAgencyId())) {
                if(orderList.getOrderStatus().equals(OrderStatus.PENDING)) {
                    orderList.setOrderStatus(OrderStatus.SHIPPING);
                    OrderList savedOrder = orderRepository.save(orderList);
                    return ApiResponse.<OrderDTO>builder()
                            .status(ErrorCode.SUCCESS)
                            .message(Message.ORDER_IS_SHIPPING)
                            .data(convertOrderToDTO(savedOrder,savedOrder.getOrderDetails().stream().toList()))
                            .build();
                }
                return ApiResponse.<OrderDTO>builder()
                        .status(ErrorCode.BAD_REQUEST)
                        .message(Message.ORDER_IS_NOT_PENDING)
                        .timestamp(new Date())
                        .build();
            }
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.UNAUTHORIZED)
                    .message(Message.AGENCY_NOT_ALLOWED)
                    .timestamp(new Date())
                    .build();
        }
        return ApiResponse.<OrderDTO>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.NOT_FOUND_ORDER)
                .timestamp(new Date())
                .build();
    }
    @Override
    public ApiResponse<OrderDTO> completeOrder(CompleteOrderRequest request){
        OrderList orderList = orderRepository.findByOrderId(request.getOrderId());
        if(orderList != null) {
            if(agencyInfoRepository.findByAccount_AccountId(orderList.getAgency().getAccountId()).get().getApplicationId().equals(request.getAgencyId())) {
                if(orderList.getOrderStatus().equals(OrderStatus.DELIVERED)) {
                    orderList.setOrderStatus(OrderStatus.COMPLETED);
                    OrderList savedOrder = orderRepository.save(orderList);
                    return ApiResponse.<OrderDTO>builder()
                            .status(ErrorCode.SUCCESS)
                            .message(Message.ORDER_IS_COMPLETED)
                            .data(convertOrderToDTO(savedOrder,savedOrder.getOrderDetails().stream().toList()))
                            .build();
                }
                return ApiResponse.<OrderDTO>builder()
                        .status(ErrorCode.BAD_REQUEST)
                        .message(Message.ORDER_IS_NOT_DELIVERED)
                        .timestamp(new Date())
                        .build();
            }
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.UNAUTHORIZED)
                    .message(Message.AGENCY_NOT_ALLOWED)
                    .timestamp(new Date())
                    .build();
        }
        return ApiResponse.<OrderDTO>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.NOT_FOUND_ORDER)
                .timestamp(new Date())
                .build();
    }

    @Override
    public ApiResponse<OrderDTO> confirmReturnOrder(Long orderId){
        OrderList orderList = orderRepository.findByOrderId(orderId);
        if(orderList != null) {
            if(orderList.getOrderStatus().equals(OrderStatus.DELIVERED)) {
                orderList.setOrderStatus(OrderStatus.RETURNED);
                //orderList.setReturnReason(request.getReturnReason());
                OrderList savedOrder = orderRepository.save(orderList);
                return ApiResponse.<OrderDTO>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.ORDER_IS_SHIPPING)
                        .data(convertOrderToDTO(savedOrder,savedOrder.getOrderDetails().stream().toList()))
                        .build();
            }
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.BAD_REQUEST)
                    .message(Message.ORDER_IS_NOT_DELIVERED)
                    .timestamp(new Date())
                    .build();
        }
        return ApiResponse.<OrderDTO>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.NOT_FOUND_ORDER)
                .timestamp(new Date())
                .build();
    }

    @Override
    public ApiResponse<OrderDTO> cancelOrder(AgencyCancelOrderRequest request){
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

            orderList.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(orderList);

            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.ORDER_CANCELLED_BY_AGENCY)
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
    public ApiResponse<AgencyInfoDTO> getAgencyInfo(){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isPresent()) {
            Account acc = account.get();
            Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount(acc);
            if (agencyInfo.isPresent() && agencyInfo.get().getApprovalStatus().getStatusCode().equals(StatusCode.STATUS_APPROVED)) {
                AgencyInfoDTO agencyInfoDTO = convertToAgencyInfoDTO(agencyInfo.get());
                return ApiResponse.<AgencyInfoDTO>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.SUCCESS)
                        .data(agencyInfoDTO)
                        .timestamp(new java.util.Date())
                        .build();
            }
            return ApiResponse.<AgencyInfoDTO>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.ACCOUNT_IS_NOT_AGENCY)
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<AgencyInfoDTO>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.AGENCY_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
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

//    public ShippingRequest convertToShippingRequest(OrderDTO orderDTO) {
//        AddressInfoDTO address = orderDTO.getAddress_info();
//
//        List<ShippingItem> items = orderDTO.getOrder_detail().stream().map(detail -> {
//            ShippingItem item = new ShippingItem();
//            item.setName(detail.getProductInfoDTO().getProduct_name());
//            item.setQuantity(detail.getQuantity());
//            item.setPrice(new BigDecimal(detail.getPrice()));
//            return item;
//        }).collect(Collectors.toList());
//
//        return ShippingRequest.builder()
//                .order_code("ORDER-" + orderDTO.getOrder_id())
//                .receiver_name(address.getFullname())
//                .receiver_phone(address.getPhone())
//                .receiver_address(address.getAddress_detail())
//                .total_amount(new BigDecimal(orderDTO.getTotalBill()))
//                .items(items)
//                .build();
//    }

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
