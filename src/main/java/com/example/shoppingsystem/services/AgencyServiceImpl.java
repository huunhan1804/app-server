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
import org.springframework.transaction.annotation.Transactional;

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
    private final OrderDetailRepository orderDetailRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final MultimediaRepository multimediaRepository;
    private final FeedbackRepository feedbackRepository;
    private final AccountService accountService;
    private final ProductServiceImpl productService;
    private final AgencyInfoRepository agencyInfoRepository;

    @Autowired
    public AgencyServiceImpl(ProductRepository productRepository, ApprovalStatusRepository approvalStatusRepository, OrderRepository orderRepository, OrderDetailRepository orderDetailRepository, AccountRepository accountRepository, CategoryRepository categoryRepository, ProductVariantRepository productVariantRepository, MultimediaRepository multimediaRepository, FeedbackRepository feedbackRepository, AccountService accountService, ProductServiceImpl productService, AgencyInfoRepository agencyInfoRepository) {
        this.productRepository = productRepository;
        this.approvalStatusRepository = approvalStatusRepository;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantRepository = productVariantRepository;
        this.multimediaRepository = multimediaRepository;
        this.feedbackRepository = feedbackRepository;
        this.accountService = accountService;
        this.productService = productService;
        this.agencyInfoRepository = agencyInfoRepository;
    }

    @Transactional
    @Override
    public ApiResponse<ProductFullDTO> createProduct(AddNewProductRequest request){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isPresent()){
            Optional<AgencyInfo> agencyInfoOpt = agencyInfoRepository.findByAccount_AccountId(account.get().getAccountId());
            if(agencyInfoOpt.isEmpty()) {
                return ApiResponse.<ProductFullDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.AGENCY_NOT_FOUND)
                        .timestamp(new Date())
                        .build();
            }
            AgencyInfo agencyInfo = agencyInfoOpt.get();

            Category category = categoryRepository.findByCategoryId(request.getCategory_id());
            if (category == null) {
                return ApiResponse.<ProductFullDTO>builder()
                        .status(ErrorCode.BAD_REQUEST)
                        .message(Message.NOT_FOUND_CATEGORY)
                        .timestamp(new Date())
                        .build();
            }

            Product product = new Product();
            product.setAgencyInfo(agencyInfo);
            product.setProductName(request.getProduct_name());
            product.setProductDescription(request.getProduct_description());
            product.setDesiredQuantity(request.getQuantity_in_stock());
            product.setInventoryQuantity(request.getQuantity_in_stock());
            product.setCategory(category);
            product.setCreatedBy(agencyInfo.getFullNameApplicant());
            product.setCreatedDate(LocalDateTime.now());

            if (request.getProduct_variant_list() != null && !request.getProduct_variant_list().isEmpty()) {
                AddProductVariantsRequest firstVariant = request.getProduct_variant_list().get(0);
                product.setListPrice(BigDecimal.valueOf(firstVariant.getList_price()));
                product.setSalePrice(getSalePrice(firstVariant.getList_price(), firstVariant.getSale_price()));
            } else {
                return ApiResponse.<ProductFullDTO>builder()
                        .status(ErrorCode.BAD_REQUEST)
                        .message("Product must have at least one variant.")
                        .timestamp(new Date())
                        .build();
            }

            product.setSoldAmount(0);
            product.setIsSale(false);
            product.setApprovalStatus(approvalStatusRepository.findApprovalStatusByStatusCode(StatusCode.STATUS_PENDING));
            Product saveProduct = productRepository.save(product);

            if(saveProduct.getProductId() != null) {
                multimediaRepository.deleteAllByProduct_ProductId(saveProduct.getProductId());
            }

            for(String image_url : request.getImage_urls()){
                multimediaRepository.save(Multimedia.builder()
                        .product(saveProduct)
                        .multimediaType(MultimediaType.IMAGE)
                        .multimediaUrl(image_url)
                        .build()
                );
            }
            for(AddProductVariantsRequest addProductVariantsRequest : request.getProduct_variant_list()){
                ProductVariant productVariant = new ProductVariant();
                productVariant.setProduct(saveProduct);
                productVariant.setListPrice(BigDecimal.valueOf(addProductVariantsRequest.getList_price()));
                productVariant.setSalePrice(getSalePrice(addProductVariantsRequest.getList_price(), addProductVariantsRequest.getSale_price()));
                productVariant.setProductVariantName(addProductVariantsRequest.getProduct_variant_name());
                productVariant.setDesiredQuantity(addProductVariantsRequest.getInventory_quantity());
                productVariant.setInventoryQuantity(addProductVariantsRequest.getInventory_quantity());
                productVariantRepository.save(productVariant);
            }
            return getFullInfoProduct(saveProduct.getProductId());
        }
        return ApiResponse.<ProductFullDTO>builder()
                .status(ErrorCode.UNAUTHORIZED)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new Date())
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<ProductFullDTO> updateProduct(UpdateProductRequest request){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isPresent()){
            Optional<AgencyInfo> agencyOpt = agencyInfoRepository.findByAccount(account.get());
            if(agencyOpt.isEmpty()) {
                return ApiResponse.<ProductFullDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.AGENCY_NOT_FOUND)
                        .timestamp(new Date())
                        .build();
            }
            AgencyInfo agencyInfo = agencyOpt.get();
            Optional<Product> productOpt = productRepository.findById(request.getProduct_id());
            if (productOpt.isEmpty() || !productOpt.get().getAgencyInfo().getApplicationId().equals(agencyInfo.getApplicationId())) {
                return ApiResponse.<ProductFullDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.PRODUCT_NOT_FOUND)
                        .timestamp(new Date())
                        .build();
            }

            Product product = productOpt.get();
            Category category = categoryRepository.findByCategoryId(request.getCategory_id());
            if (category == null) {
                return ApiResponse.<ProductFullDTO>builder()
                        .status(ErrorCode.BAD_REQUEST)
                        .message("Category not found.")
                        .timestamp(new Date())
                        .build();
            }

            product.setProductName(request.getProduct_name());
            product.setProductDescription(request.getProduct_description());
            product.setInventoryQuantity(request.getQuantity_in_stock());
            product.setCategory(category);
            product.setUpdatedBy(account.get().getUsername());
            product.setUpdatedDate(LocalDateTime.now());

            if (request.getProduct_variant_list() != null && !request.getProduct_variant_list().isEmpty()) {
                UpdateProductVariantsRequest firstVariant = request.getProduct_variant_list().get(0);
                if (firstVariant.getList_price() != null) {
                    product.setListPrice(BigDecimal.valueOf(firstVariant.getList_price()));
                }
                if (firstVariant.getSale_price() != null) {
                    product.setSalePrice(getSalePrice(firstVariant.getSale_price(), firstVariant.getSale_price()));
                }
            }

            product.setApprovalStatus(approvalStatusRepository.findApprovalStatusByStatusCode(StatusCode.STATUS_PENDING));

            multimediaRepository.deleteAllByProduct_ProductId(product.getProductId());
            for(String imageUrl : request.getImage_urls()){
                multimediaRepository.save(Multimedia.builder()
                        .product(product)
                        .multimediaType(MultimediaType.IMAGE)
                        .multimediaUrl(imageUrl)
                        .build());
            }

            List<ProductVariant> existingVariants = productVariantRepository.findAllByProduct_ProductId(product.getProductId());
            Map<Long, ProductVariant> existingVariantMap = existingVariants.stream()
                    .collect(Collectors.toMap(ProductVariant::getProductVariantId, v -> v));

            List<UpdateProductVariantsRequest> updatedVariantsRequest = request.getProduct_variant_list();

            List<ProductVariant> variantsToSave = new ArrayList<>();
            Set<Long> variantsToKeep = new HashSet<>();

            for(UpdateProductVariantsRequest variantRequest : updatedVariantsRequest) {
                ProductVariant variant;
                if(variantRequest.getProduct_variant_id() != null && existingVariantMap.containsKey(variantRequest.getProduct_variant_id())) {
                    variant = existingVariantMap.get(variantRequest.getProduct_variant_id());
                    variantsToKeep.add(variant.getProductVariantId());
                } else {
                    variant = new ProductVariant();
                    variant.setProduct(product);
                }

                variant.setProductVariantName(variantRequest.getProduct_variant_name());
                if (variantRequest.getList_price() != null) {
                    variant.setListPrice(BigDecimal.valueOf(variantRequest.getList_price()));
                }
                if (variantRequest.getSale_price() != null) {
                    variant.setSalePrice(getSalePrice(variantRequest.getSale_price(), variantRequest.getSale_price()));
                }
                variant.setInventoryQuantity(variantRequest.getInventory_quantity());
                // variant.(variantRequest.getSold_amount());
                variantsToSave.add(variant);
            }

            existingVariants.stream()
                    .filter(v -> !variantsToKeep.contains(v.getProductVariantId()))
                    .forEach(productVariantRepository::delete);

            productVariantRepository.saveAll(variantsToSave);

            Product savedProduct = productRepository.save(product);
            return getFullInfoProduct(savedProduct.getProductId());
        }
        return ApiResponse.<ProductFullDTO>builder()
                .status(ErrorCode.UNAUTHORIZED)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new Date())
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<Void> deleteProduct(Long product_id){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isPresent()){
            Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount(account.get());
            if (agencyInfo.isPresent()) {
                Optional<Product> productOpt = productRepository.findById(product_id);
                if (productOpt.isEmpty() || !productOpt.get().getAgencyInfo().getApplicationId().equals(agencyInfo.get().getApplicationId())) {
                    return ApiResponse.<Void>builder()
                            .status(ErrorCode.NOT_FOUND)
                            .message(Message.PRODUCT_NOT_FOUND)
                            .timestamp(new Date())
                            .build();
                }
                Product product = productOpt.get();
                product.setIsSale(false);
                productRepository.save(product);
                return ApiResponse.<Void>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.DELETE_PRODUCT_SUCCESS)
                        .timestamp(new Date())
                        .build();
            }
            return ApiResponse.<Void>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.AGENCY_INFO_NOT_FOUND)
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<Void>builder()
                .status(ErrorCode.UNAUTHORIZED)
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
                List<Product> results;
                if (status_code == null || status_code.isEmpty()) {
                    results = productRepository.findByAgencyInfo(agencyInfo.get());
                } else {
                    results = productRepository.findAllByAgencyInfoAndApprovalStatus_StatusCode(agencyInfo.get(), status_code);
                    results.sort(Comparator.comparing(Product::getProductName));
                }

                List<ProductInfoDTO> productDTOs = new ArrayList<>();
                for(Product product : results){
                    productDTOs.add(convertToProductInfoDTO(product, product.getProductVariants(), feedbackRepository.findByProduct_ProductId(product.getProductId()), multimediaRepository.findAllByProduct_ProductId(product.getProductId())));
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

//    @Override
//    public ApiResponse<ProductInfoDTO> disableSellingProduct(Long product_id){
//        Product product = productRepository.findByProductId(product_id);
//        if(product != null){
//            if(product.getIsSale()){
//                product.setIsSale(false);
//                Product savedProduct = productRepository.save(product);
//                ProductInfoDTO productInfoDTO = convertToProductInfoDTO(savedProduct, product.getProductVariants(), null, multimediaRepository.findAllByProduct_ProductId(savedProduct.getProductId()));
//                return ApiResponse.<ProductInfoDTO>builder()
//                        .status(ErrorCode.SUCCESS)
//                        .message(Message.DISABLED_PRODUCT_SUCCESS)
//                        .data(productInfoDTO)
//                        .timestamp(new java.util.Date())
//                        .build();
//            }
//            return ApiResponse.<ProductInfoDTO>builder()
//                    .status(ErrorCode.CONFLICT)
//                    .message(Message.PRODUCT_IS_NOT_SELLING)
//                    .timestamp(new java.util.Date())
//                    .build();
//        }
//        return ApiResponse.<ProductInfoDTO>builder()
//                .status(ErrorCode.NOT_FOUND)
//                .message(Message.NOT_FOUND_PRODUCT)
//                .timestamp(new java.util.Date())
//                .build();
//    }

    @Override
    public ApiResponse<ProductFullDTO> getFullInfoProduct(long productId) {
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isPresent()){
            Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount(account.get());
            if(agencyInfo.isEmpty()) {
                return ApiResponse.<ProductFullDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.AGENCY_NOT_FOUND)
                        .timestamp(new Date())
                        .build();
            }
            Optional<Product> product = productRepository.findById(productId);
            if(product.isPresent() && product.get().getAgencyInfo().getApplicationId().equals(agencyInfo.get().getApplicationId())){
                return ApiResponse.<ProductFullDTO>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.SUCCESS)
                        .data(convertToProductFullDTO(product.get()))
                        .timestamp(new java.util.Date())
                        .build();
            }
        }
        return ApiResponse.<ProductFullDTO>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
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
        productInfoDTO.setProduct_price(Regex.formatPriceToVND(product.getListPrice()));
        productInfoDTO.setRating(calculateAverageRating(product));
        productInfoDTO.setProduct_description(product.getProductDescription());
        productInfoDTO.setQuantity_in_stock(product.getDesiredQuantity());
        productInfoDTO.setSold_amount(product.getSoldAmount());

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
        productInfoDTO.setApproval_status(ApprovalStatusDTO.builder()
                .statusId(product.getApprovalStatus().getStatusId())
                .statusCode(product.getApprovalStatus().getStatusCode())
                .statusName(product.getApprovalStatus().getStatusName())
                .build());

        if (product.getCategory() != null) {
            productInfoDTO.setCategory(CategoryDTO.builder()
                    .categoryId(product.getCategory().getCategoryId())
                    .categoryName(product.getCategory().getCategoryName())
                    .categoryDescription(product.getCategory().getCategoryDescription())
                    .build());
        } else {
            productInfoDTO.setCategory(null);
        }

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
        productFullDTO.setProduct_price(Regex.formatPriceToVND(product.getListPrice()));

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
    public ApiResponse<List<OrderDTO>> getOrders(){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isPresent()) {
            Optional<AgencyInfo> agencyInfoOpt = agencyInfoRepository.findByAccount(account.get());
            if(agencyInfoOpt.isEmpty() || !agencyInfoOpt.get().getApprovalStatus().getStatusCode().equals(StatusCode.STATUS_APPROVED)) {
                return ApiResponse.<List<OrderDTO>>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.ACCOUNT_IS_NOT_AGENCY)
                        .timestamp(new java.util.Date())
                        .build();
            }
            List<OrderList> orderLists = orderRepository.findAllByAgency_ApplicationId(agencyInfoOpt.get().getApplicationId());
            if(orderLists.isEmpty()) {
                return ApiResponse.<List<OrderDTO>>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.NOT_FOUND_ORDER)
                        .timestamp(new java.util.Date())
                        .build();
            }

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

    @Override
    public ApiResponse<OrderDTO> confirmOrder(ConfirmOrderRequest request){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isPresent()) {
            Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount(account.get());
            if(agencyInfo.isEmpty() || !agencyInfo.get().getApprovalStatus().getStatusCode().equals(StatusCode.STATUS_APPROVED)) {
                return ApiResponse.<OrderDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.ACCOUNT_IS_NOT_AGENCY)
                        .timestamp(new java.util.Date())
                        .build();
            }
            OrderList orderList = orderRepository.findByOrderId(request.getOrderId());
            if(orderList == null) {
                return ApiResponse.<OrderDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.NOT_FOUND_ORDER)
                        .timestamp(new java.util.Date())
                        .build();
            }
            if(!isAgencyOrders(orderList, account.get())){
                return ApiResponse.<OrderDTO>builder()
                        .status(ErrorCode.FORBIDDEN)
                        .message(Message.AGENCY_NOT_ALLOWED)
                        .timestamp(new Date())
                        .build();
            }

            if(!orderList.getOrderStatus().equals(OrderStatus.PENDING)) {
                return ApiResponse.<OrderDTO>builder()
                        .status(ErrorCode.BAD_REQUEST)
                        .message(Message.ORDER_IS_NOT_PENDING)
                        .timestamp(new java.util.Date())
                        .build();
            }

            orderList.setOrderStatus(OrderStatus.SHIPPING);
            orderRepository.save(orderList);
            List<OrderDetail> orderDetails = orderDetailRepository.findAllByOrderList_OrderId(orderList.getOrderId());
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.CONFIRM_ORDER_SUCCESS)
                    .data(convertOrderToDTO(orderList, orderDetails))
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<OrderDTO>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<String> getOrderStatus(Long order_id){
        OrderList orderList = orderRepository.findByOrderId(order_id);
        if(orderList != null) {
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
    public ApiResponse<List<OrderDTO>> getListOfOrdersByStatus(String status){ // THAY ĐỔI: tham số là String
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isPresent()) {
            Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount(account.get());
            if(agencyInfo.isEmpty() || !agencyInfo.get().getApprovalStatus().getStatusCode().equals(StatusCode.STATUS_APPROVED)) {
                return ApiResponse.<List<OrderDTO>>builder()
                        .status(ErrorCode.FORBIDDEN)
                        .message(Message.ACCOUNT_IS_NOT_AGENCY)
                        .timestamp(new java.util.Date())
                        .build();
            }
            List<OrderList> orderLists = orderRepository.findAllByAgency_ApplicationId(agencyInfoRepository.findByAccount(account.get()).get().getApplicationId());
            if(orderLists.isEmpty()) {
                return ApiResponse.<List<OrderDTO>>builder()
                        .status(ErrorCode.SUCCESS) // Trả về thành công với danh sách rỗng
                        .message(Message.FETCHING_ORDER_SUCCESS)
                        .data(new ArrayList<>())
                        .timestamp(new java.util.Date())
                        .build();
            }
            List<OrderDTO> orderDTOS = new ArrayList<>();
            for(OrderList orderList : orderLists) {
                // THAY ĐỔI: So sánh với `status` từ tham số
                if(orderList.getOrderStatus().equals(OrderStatus.valueOf(status))) {
                    OrderDTO orderDTO = convertOrderToDTO(orderList, orderDetailRepository.findAllByOrderList_OrderId(orderList.getOrderId()));
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
                .status(ErrorCode.NOT_FOUND)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<OrderDTO> shipOrder(ShippingRequest request){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isEmpty()) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.ACCOUNT_NOT_FOUND)
                    .timestamp(new Date())
                    .build();
        }
        Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount(account.get());
        if(agencyInfo.isEmpty() || !agencyInfo.get().getApprovalStatus().getStatusCode().equals(StatusCode.STATUS_APPROVED)) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.FORBIDDEN)
                    .message(Message.ACCOUNT_IS_NOT_AGENCY)
                    .timestamp(new Date())
                    .build();
        }
        OrderList orderList = orderRepository.findByOrderId(request.getOrderId());
        if(orderList != null) {
            if(!isAgencyOrders(orderList, account.get())){
                return ApiResponse.<OrderDTO>builder()
                        .status(ErrorCode.FORBIDDEN)
                        .message(Message.AGENCY_NOT_ALLOWED)
                        .timestamp(new Date())
                        .build();
            }
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
                .status(ErrorCode.NOT_FOUND)
                .message(Message.NOT_FOUND_ORDER)
                .timestamp(new Date())
                .build();
    }

    @Override
    public ApiResponse<OrderDTO> completeOrder(CompleteOrderRequest request){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isEmpty()) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.ACCOUNT_NOT_FOUND)
                    .timestamp(new Date())
                    .build();
        }
        Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount(account.get());
        if(agencyInfo.isEmpty() || !agencyInfo.get().getApprovalStatus().getStatusCode().equals(StatusCode.STATUS_APPROVED)) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.FORBIDDEN)
                    .message(Message.ACCOUNT_IS_NOT_AGENCY)
                    .timestamp(new Date())
                    .build();
        }
        OrderList orderList = orderRepository.findByOrderId(request.getOrderId());
        if(orderList != null) {
            if(!isAgencyOrders(orderList, account.get())){
                return ApiResponse.<OrderDTO>builder()
                        .status(ErrorCode.FORBIDDEN)
                        .message(Message.AGENCY_NOT_ALLOWED)
                        .timestamp(new Date())
                        .build();
            }
            if(orderList.getOrderStatus().equals(OrderStatus.SHIPPING)) {
                orderList.setOrderStatus(OrderStatus.DELIVERED);
                orderList.setUpdatedBy(agencyInfo.get().getShopName());
                orderList.setUpdatedDate(LocalDateTime.now());
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
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(account.isEmpty()) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.ACCOUNT_NOT_FOUND)
                    .timestamp(new Date())
                    .build();
        }
        Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount(account.get());
        if(agencyInfo.isEmpty() || !agencyInfo.get().getApprovalStatus().getStatusCode().equals(StatusCode.STATUS_APPROVED)) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.FORBIDDEN)
                    .message(Message.ACCOUNT_IS_NOT_AGENCY)
                    .timestamp(new Date())
                    .build();
        }
        OrderList orderList = orderRepository.findByOrderId(request.getOrderId());
        if(orderList == null) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.NOT_FOUND_ORDER)
                    .timestamp(new java.util.Date())
                    .build();
        }
        if(!isAgencyOrders(orderList, account.get())){
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.FORBIDDEN)
                    .message(Message.AGENCY_NOT_ALLOWED)
                    .timestamp(new Date())
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
                .data(convertOrderToDTO(orderList, orderDetailRepository.findAllByOrderList_OrderId(orderList.getOrderId())))
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

    @Override
    @Transactional
    public ApiResponse<ProductInfoDTO> toggleProductSaleStatus(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return ApiResponse.<ProductInfoDTO>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.NOT_FOUND_PRODUCT)
                    .timestamp(new Date())
                    .build();
        }

        Product product = productOpt.get();
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isEmpty() || !product.getAgencyInfo().getAccount().equals(account.get())) {
            return ApiResponse.<ProductInfoDTO>builder()
                    .status(ErrorCode.FORBIDDEN)
                    .message(Message.AGENCY_NOT_ALLOWED)
                    .timestamp(new Date())
                    .build();
        }

        if (!product.getApprovalStatus().getStatusCode().equals(StatusCode.STATUS_APPROVED)) {
            return ApiResponse.<ProductInfoDTO>builder()
                    .status(ErrorCode.BAD_REQUEST)
                    .message(Message.PRODUCT_IS_NOT_APPROVED)
                    .timestamp(new Date())
                    .build();
        }

        product.setIsSale(!product.getIsSale());
        Product savedProduct = productRepository.save(product);
        ProductInfoDTO productInfoDTO = convertToProductInfoDTO(savedProduct, savedProduct.getProductVariants(), null, multimediaRepository.findAllByProduct_ProductId(savedProduct.getProductId()));

        return ApiResponse.<ProductInfoDTO>builder()
                .status(ErrorCode.SUCCESS)
                .message("Toggle sale status successful")
                .data(productInfoDTO)
                .timestamp(new Date())
                .build();
    }

    @Override
    public ApiResponse<ShopInfoDTO> getShopInfo(){
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isPresent()) {
            Account acc = account.get();
            Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount(acc);
            if (agencyInfo.isPresent() && agencyInfo.get().getApprovalStatus().getStatusCode().equals(StatusCode.STATUS_APPROVED)) {
                ShopInfoDTO shopInfoDTO = new ShopInfoDTO();
                shopInfoDTO.setShopId(agencyInfo.get().getApplicationId());
                shopInfoDTO.setShopName(agencyInfo.get().getShopName());
                shopInfoDTO.setShopAvatar(Objects.requireNonNull(multimediaRepository.findByAccount(acc).orElse(null)).getMultimediaUrl());
                shopInfoDTO.setShopAddress(agencyInfo.get().getShopAddressDetail());
                shopInfoDTO.setShopPhone(agencyInfo.get().getShopPhone());
                return ApiResponse.<ShopInfoDTO>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.SUCCESS)
                        .data(shopInfoDTO)
                        .timestamp(new java.util.Date())
                        .build();
            }
            return ApiResponse.<ShopInfoDTO>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.AGENCY_NOT_FOUND)
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<ShopInfoDTO>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();

    }


    private boolean isAgencyOrders(OrderList orderList, Account agency) {
        return orderList.getAgency().equals(agencyInfoRepository.findByAccount(agency).get());
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

    public BigDecimal getSalePrice(double listPrice, double salePrice) {
        if(salePrice != 0){
            return BigDecimal.valueOf(salePrice);
        }
        return BigDecimal.valueOf(listPrice);
    }
    // *** THÊM HÀM MỚI NÀY ***
    @Override
    public ApiResponse<OrderDTO> respondToReturnRequest(AgencyReturnResponseRequest request) {
        Optional<Account> accountOpt = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (accountOpt.isEmpty()) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.FORBIDDEN).message(Message.ACCOUNT_NOT_FOUND).build();
        }

        Optional<OrderList> orderOpt = orderRepository.findById(request.getOrderId());
        if (orderOpt.isEmpty()) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.NOT_FOUND).message(Message.NOT_FOUND_ORDER).build();
        }

        OrderList order = orderOpt.get();

        // Kiểm tra xem Agency này có quyền trên đơn hàng này không
        if (!isAgencyOrders(order, accountOpt.get())) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.FORBIDDEN).message(Message.AGENCY_NOT_ALLOWED).build();
        }

        // Kiểm tra trạng thái đơn hàng phải là RETURNED
        if (!order.getOrderStatus().equals(OrderStatus.RETURNED)) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.BAD_REQUEST)
                    .message("Order is not in RETURNED state.")
                    .build();
        }

        // Cập nhật lại lý do theo yêu cầu
        order.setReturnReason(request.getReason());
        order.setUpdatedBy(accountOpt.get().getUsername());
        order.setUpdatedDate(LocalDateTime.now());
        OrderList savedOrder = orderRepository.save(order);

        // Trả về thông tin đơn hàng đã được cập nhật
        return getFullOrderInformation(savedOrder.getOrderId());
    }

    // Tạo một hàm helper để lấy đầy đủ thông tin chi tiết đơn hàng
    private ApiResponse<OrderDTO> getFullOrderInformation(Long orderId) {
        Optional<OrderList> orderListOpt = orderRepository.findById(orderId);
        if (orderListOpt.isEmpty()) {
            return ApiResponse.<OrderDTO>builder()
                    .status(ErrorCode.NOT_FOUND).message(Message.NOT_FOUND_ORDER).build();
        }
        OrderList orderList = orderListOpt.get();
        List<OrderDetail> orderDetails = orderDetailRepository.findAllByOrderList_OrderId(orderList.getOrderId());
        OrderDTO orderDTO = convertOrderToDTO(orderList, orderDetails);

        return ApiResponse.<OrderDTO>builder()
                .status(ErrorCode.SUCCESS)
                .message("Successfully updated return reason.")
                .data(orderDTO)
                .build();
    }

}
