package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.Message;
import com.example.shoppingsystem.constants.Regex;
import com.example.shoppingsystem.constants.StatusCode;
import com.example.shoppingsystem.dtos.*;
import com.example.shoppingsystem.entities.*;
import com.example.shoppingsystem.enums.Rating;
import com.example.shoppingsystem.repositories.*;
import com.example.shoppingsystem.requests.AddFeedbackRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.ProductService;
import net.sf.ehcache.util.ProductInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    private final FeedbackRepository feedbackRepository;
    private final MultimediaRepository multimediaRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final AgencyInfoRepository agencyInfoRepository;
    private final Random random = new Random();
    private final AccountRepository accountRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ProductVariantRepository productVariantRepository, CategoryRepository categoryRepository, FeedbackRepository feedbackRepository, MultimediaRepository multimediaRepository, OrderDetailRepository orderDetailRepository, AgencyInfoRepository agencyInfoRepository, AccountRepository accountRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.categoryRepository = categoryRepository;
        this.feedbackRepository = feedbackRepository;
        this.multimediaRepository = multimediaRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.agencyInfoRepository = agencyInfoRepository;
        this.accountRepository = accountRepository;
    }


    @Override
    public ApiResponse<List> getListProductByParentCategory(long listProductByParentCategoryRequest) {
        List<Category> categoryList = categoryRepository.findAllByParentCategory_ParentCategoryId(listProductByParentCategoryRequest);
        List<ProductBasicDTO> productList = categoryList.stream()
                .flatMap(this::getProductBasicDTOsByCategory)
                .collect(Collectors.toList());

        return ApiResponse.<List>builder()
                .status(ErrorCode.SUCCESS)
                .message(Message.SUCCESS)
                .data(productList)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<ProductInfoDTO> getInfoProduct(long productId) {
        Optional<Product> product = productRepository.findById(productId);
        if(product.isPresent()){
            if(product.get().getIsSale() == false){
                return ApiResponse.<ProductInfoDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.NOT_FOUND)
                        .timestamp(new java.util.Date())
                        .build();
            }

            if (!product.get().getApprovalStatus().getStatusCode().equals(StatusCode.STATUS_APPROVED)) {
                return ApiResponse.<ProductInfoDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.NOT_FOUND)
                        .timestamp(new java.util.Date())
                        .build();
            }
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

    @Override
    public Optional<Product> getProductByID(long productId) {
        return productRepository.findById(productId);
    }

    @Override
    public Optional<ProductVariant> getProductVariantByID(long productVariantId) {
        return productVariantRepository.findById(productVariantId);
    }

    @Override
    public ApiResponse<List> getListBestSellerProduct() {
        List<Product> productList = productRepository.findTop10ByOrderBySoldAmountDesc();
        List<ProductBasicDTO> productBasicDTOS = productList.stream()
                .map(product -> convertToProductBasicDTO(product)).collect(Collectors.toList());

        return ApiResponse.<List>builder()
                .status(ErrorCode.SUCCESS)
                .message(Message.SUCCESS)
                .data(productBasicDTOS)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<List> getListBestOrderProduct() {
        List<Object[]> results = orderDetailRepository.findBestOrderProductsJPQL();
        List<Product> productList = new ArrayList<>();
        for (Object[] result : results) {
            Long productId = (Long) result[0];
            Optional<Product> product = productRepository.findById(productId);
            product.ifPresent(productList::add);
        }
        List<ProductBasicDTO> productBasicDTOS = productList.stream()
                .map(this::convertToProductBasicDTO).collect(Collectors.toList());
        return ApiResponse.<List>builder()
                .status(ErrorCode.SUCCESS)
                .message(Message.SUCCESS)
                .data(productBasicDTOS)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<List<ProductBasicDTO>> getListRelatedProduct(long productId) {
        List<Product> relatedProducts = new ArrayList<>();

        // Lấy 3 sản phẩm bán chạy nhất
        List<Product> bestSellers = productRepository.findTop3ByOrderBySoldAmountDesc().stream()
                .filter(p -> p.getApprovalStatus() != null &&
                        StatusCode.STATUS_APPROVED.equals(p.getApprovalStatus().getStatusCode()))
                .toList();
        relatedProducts.addAll(bestSellers);

        // Lấy 3 sản phẩm mới nhất
        List<Product> latestProducts = productRepository.findTop3ByOrderByProductIdDesc().stream()
                .filter(p -> p.getApprovalStatus() != null &&
                        StatusCode.STATUS_APPROVED.equals(p.getApprovalStatus().getStatusCode()))
                .toList();

        relatedProducts.addAll(latestProducts);

        // Lấy 4 sản phẩm ngẫu nhiên từ category ngẫu nhiên
        List<Category> categories = categoryRepository.findAll();
        if (!categories.isEmpty()) {
            Category randomCategory = categories.get(random.nextInt(categories.size()));
            List<Product> randomProducts = productRepository.findByCategory_CategoryId(randomCategory.getCategoryId())
                    .stream()
                    .filter(p -> p.getApprovalStatus() != null && StatusCode.STATUS_APPROVED.equals(p.getApprovalStatus().getStatusCode()))
                    .limit(4)
                    .toList();
            relatedProducts.addAll(randomProducts);
        }

        // Loại bỏ duplicate và giới hạn 10 sản phẩm
        List<Product> uniqueProducts = relatedProducts.stream()
                .distinct()
                .limit(10)
                .toList();

        List<ProductBasicDTO> productBasicDTOS = uniqueProducts.stream()
                .map(this::convertToProductBasicDTO)
                .collect(Collectors.toList());

        return ApiResponse.<List<ProductBasicDTO>>builder()
                .status(ErrorCode.SUCCESS)
                .message(Message.SUCCESS)
                .data(productBasicDTOS)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<List<ProductBasicDTO>> searchProductsByKeyword(String keyword) {
        List<Product> products = productRepository.findByProductNameContainingIgnoreCase(keyword);

        List<Product> approvedProducts = products.stream()
                .filter(p -> p.getApprovalStatus() != null &&
                        StatusCode.STATUS_APPROVED.equals(p.getApprovalStatus().getStatusCode()))
                .toList();


        if (approvedProducts.isEmpty()) {
            return ApiResponse.<List<ProductBasicDTO>>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.NOT_FOUND)
                    .data(new ArrayList<>())
                    .timestamp(new java.util.Date())
                    .build();
        }

        List<ProductBasicDTO> productBasicDTOs = approvedProducts.stream()
                .map(this::convertToProductBasicDTO)
                .collect(Collectors.toList());

        return ApiResponse.<List<ProductBasicDTO>>builder()
                .status(ErrorCode.SUCCESS)
                .message(Message.SUCCESS)
                .data(productBasicDTOs)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<List> getListProductByCategory(Long categoryId) {
        if (categoryId == 0) {
            List<Product> allProducts = productRepository.findAll();
            List<Product> approvedProducts = allProducts.stream()
                    .filter(p -> p.getApprovalStatus() != null &&
                            StatusCode.STATUS_APPROVED.equals(p.getApprovalStatus().getStatusCode()))
                    .toList();

            List<ProductBasicDTO> productBasicDTOs = approvedProducts.stream()
                    .map(this::convertToProductBasicDTO)
                    .collect(Collectors.toList());

            return ApiResponse.<List>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(productBasicDTOs)
                    .timestamp(new java.util.Date())
                    .build();
        }
        Optional<Category> category = categoryRepository.findById(categoryId);
        if(category.isPresent()){
            return ApiResponse.<List>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(getProductBasicDTOsByCategory(category.get()).toList())
                    .timestamp(new java.util.Date())
                    .build();
        } else {
            return ApiResponse.<List>builder()
                    .status(ErrorCode.BAD_REQUEST)
                    .message(Message.NOT_FOUND_CATEGORY)
                    .timestamp(new java.util.Date())
                    .build();
        }
    }

    @Override
    public ApiResponse<List> getListProductByAgency(Long agencyId){
        Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByApplicationId(agencyId);
        if(agencyInfo.isPresent()){
            return ApiResponse.<List>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(getProductBasicDTOsByAgency(agencyInfo.get()).toList())
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<List>builder()
                .status(ErrorCode.BAD_REQUEST)
                .message(Message.AGENCY_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<FeedbackDTO> addFeedback(AddFeedbackRequest request) {
        // Logic giả định lấy người dùng từ context bảo mật, bạn cần tùy chỉnh theo cách bạn quản lý Authentication
        Account account = getLoggedInUser();

        // 1. Kiểm tra sản phẩm có tồn tại không
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 2. Tạo entity Feedback mới
        Feedback newFeedback = new Feedback();
        newFeedback.setProduct(product);
        newFeedback.setAccount(account);
        newFeedback.setRating(Rating.fromValue(request.getRating())); // SỬ DỤNG ENUM RATING
        newFeedback.setComment(request.getComment());

        // 3. Lưu feedback vào database
        Feedback savedFeedback = feedbackRepository.save(newFeedback);


        // 5. Trả về response
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setUser_name(savedFeedback.getAccount().getFullname());
        feedbackDTO.setRating(savedFeedback.getRating().getValue());
        feedbackDTO.setComment(savedFeedback.getComment());

        return ApiResponse.<FeedbackDTO>builder()
                .status(ErrorCode.SUCCESS)
                .message(Message.SUCCESS)
                .data(feedbackDTO)
                .timestamp(new java.util.Date())
                .build();
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
        productInfoDTO.setShop_info(convertToShopInfoDTO(product.getAgencyInfo()));

        return productInfoDTO;
    }

    private ShopInfoDTO convertToShopInfoDTO(AgencyInfo agencyInfo) {
        ShopInfoDTO shopInfoDTO = new ShopInfoDTO();
        shopInfoDTO.setShopId(agencyInfo.getApplicationId());
        shopInfoDTO.setShopName(agencyInfo.getShopName());
        shopInfoDTO.setShopPhone(agencyInfo.getShopPhone());
        shopInfoDTO.setShopEmail(agencyInfo.getShopEmail());
        shopInfoDTO.setShopAddress(agencyInfo.getShopAddressDetail());

        Optional<Multimedia> multimedia = multimediaRepository.findByAccount(agencyInfo.getAccount());
        shopInfoDTO.setShopAvatar(multimedia.map(Multimedia::getMultimediaUrl).orElse(null));

//        List<Product> products = productRepository.findAllByAgencyInfoAndAgencyInfo_ApprovalStatus_StatusCode(agencyInfo, StatusCode.STATUS_APPROVED);
//        List<ProductInfoDTO> productInfos = new ArrayList<>();
//        for(Product product : products){
//            if (product.getIsSale()){
//                ProductInfoDTO productInfoDTO = convertToProductInfoDTO(product, product.getProductVariants(),feedbackRepository.findByProduct_ProductId(product.getProductId()), multimediaRepository.findAllByProduct_ProductId(product.getProductId()));
//                productInfos.add(productInfoDTO);
//            }
//        }
//        shopInfoDTO.setProducts(productInfos);
        return shopInfoDTO;
    }

    private Stream<ProductBasicDTO> getProductBasicDTOsByCategory(Category category) {
        List<Product> productsInCategory = productRepository.findByCategory_CategoryIdAndApprovalStatus_StatusCode(category.getCategoryId(), StatusCode.STATUS_APPROVED);
        return productsInCategory.stream()
                .map(this::convertToProductBasicDTO);
    }

    private Stream<ProductBasicDTO> getProductBasicDTOsByAgency(AgencyInfo agencyInfo) {
        List<Product> productInsAgency = productRepository.findAllByAgencyInfoAndApprovalStatus_StatusCode(agencyInfo, StatusCode.STATUS_APPROVED);
        return productInsAgency.stream()
                .map(this::convertToProductBasicDTO);
    }

    private ProductBasicDTO convertToProductBasicDTO(Product product) {
        ProductBasicDTO productBasicDTO = new ProductBasicDTO();
        productBasicDTO.setProduct_id(product.getProductId());
        productBasicDTO.setImage_url(findImageProduct(product));
        productBasicDTO.setProduct_name(product.getProductName());
        productBasicDTO.setProduct_price(Regex.formatPriceToVND(product.getSalePrice()));
        productBasicDTO.setRating(calculateAverageRating(product));
        return productBasicDTO;
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
        Optional<Multimedia> multimedia = multimediaRepository.findFirstByProduct_ProductId(product.getProductId());
        return multimedia.map(Multimedia::getMultimediaUrl).orElse(null);
    }

    public String findImageProductVariant(ProductVariant productVariant) {
        Optional<Multimedia> multimedia = multimediaRepository.findFirstByProductVariant_ProductVariantId(productVariant.getProductVariantId());
        return multimedia.map(Multimedia::getMultimediaUrl).orElse(null);
    }

    private Account getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        String username = authentication.getName();
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found for authenticated user: " + username));
    }

}
