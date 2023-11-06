package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.Message;
import com.example.shoppingsystem.constants.Regex;
import com.example.shoppingsystem.dtos.FeedbackDTO;
import com.example.shoppingsystem.dtos.ProductBasicDTO;
import com.example.shoppingsystem.dtos.ProductInfoDTO;
import com.example.shoppingsystem.dtos.ProductVariantDTO;
import com.example.shoppingsystem.entities.*;
import com.example.shoppingsystem.repositories.*;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ProductVariantRepository productVariantRepository, CategoryRepository categoryRepository, FeedbackRepository feedbackRepository, MultimediaRepository multimediaRepository, OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.categoryRepository = categoryRepository;
        this.feedbackRepository = feedbackRepository;
        this.multimediaRepository = multimediaRepository;
        this.orderDetailRepository = orderDetailRepository;
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
        List<Multimedia> multimediaProduct = multimediaRepository.findAllByProduct_ProductId(product.getProductId());
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
        List<Product> productList = productRepository.findListBestSellerProduct();
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
        List<Object[]> results = orderDetailRepository.findBestOrderProducts();
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
        Optional<Product> productOpt = productRepository.findById(productId);
        if(productOpt.isPresent()) {
            Product product = productOpt.get();
            Category productCategory = product.getCategory();
            List<Product> relatedProducts = productRepository.findByCategoryAndProductIdNot(productCategory, productId);
            List<ProductBasicDTO> productBasicDTOS = relatedProducts.stream()
                    .map(this::convertToProductBasicDTO).collect(Collectors.toList());
            return ApiResponse.<List<ProductBasicDTO>>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(productBasicDTOS)
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<List<ProductBasicDTO>>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.NOT_FOUND)
                .data(null)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<List<ProductBasicDTO>> searchProductsByKeyword(String keyword) {
        List<Product> products = productRepository.findByProductNameContainingIgnoreCase(keyword);

        if (products.isEmpty()) {
            return ApiResponse.<List<ProductBasicDTO>>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.NOT_FOUND)
                    .data(new ArrayList<>())
                    .timestamp(new java.util.Date())
                    .build();
        }

        List<ProductBasicDTO> productBasicDTOs = products.stream()
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

    private Stream<ProductBasicDTO> getProductBasicDTOsByCategory(Category category) {
        List<Product> productsInCategory = productRepository.findByCategory_CategoryId(category.getCategoryId());
        return productsInCategory.stream()
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
        Optional<Multimedia> multimedia = multimediaRepository.findByProduct_ProductId(product.getProductId());
        return multimedia.map(Multimedia::getMultimediaUrl).orElse(null);
    }

    public String findImageProductVariant(ProductVariant productVariant) {
        Optional<Multimedia> multimedia = multimediaRepository.findAllByProductVariant_ProductVariantId(productVariant.getProductVariantId());
        return multimedia.map(Multimedia::getMultimediaUrl).orElse(null);
    }


}
