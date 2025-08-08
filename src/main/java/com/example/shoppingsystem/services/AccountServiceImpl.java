package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.*;
import com.example.shoppingsystem.dtos.*;
import com.example.shoppingsystem.entities.*;
import com.example.shoppingsystem.enums.MultimediaType;
import com.example.shoppingsystem.repositories.*;
import com.example.shoppingsystem.requests.*;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.AccountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AccountServiceImpl implements AccountService {
    private static final Logger logger = LogManager.getLogger(AccountServiceImpl.class);
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final MultimediaRepository multimediaRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceImpl productService;
    private final AgencyInfoRepository agencyInfoRepository;
    private final ApprovalStatusRepository approvalStatusRepository;

    @Autowired
    public AccountServiceImpl(PasswordEncoder passwordEncoder, AccountRepository accountRepository, RoleRepository roleRepository, MultimediaRepository multimediaRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, ProductServiceImpl productService, AgencyInfoRepository agencyInfoRepository, ApprovalStatusRepository approvalStatusRepository) {
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.multimediaRepository = multimediaRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.agencyInfoRepository = agencyInfoRepository;
        this.approvalStatusRepository = approvalStatusRepository;
    }

    @Override
    public Account createAccountWithSocialLogin(String email, String password, String fullname, String imageLink) {
        logger.info(String.format(LogMessage.LOG_PROCESSING_CREATE_NEW_ACCOUNT, email));
        String username = generateUsername(email);
        if (username == null) {
            logger.error(String.format(LogMessage.LOG_INVALID_LOGIN_ID, email));
            return null;
        }
        return createAccount(username, email, "", password, fullname, imageLink);
    }


    @Override
    public Account createAccountWithEmail(String email, String password, String fullname) {
        if ((accountRepository.findByEmail(email)).isPresent()) {
            logger.error(String.format(LogMessage.LOG_ACCOUNT_EXIST_EMAIL, email));
            return null;
        }

        String username = generateUsername(email);
        if (username == null) {
            logger.error(String.format(LogMessage.LOG_INVALID_LOGIN_ID, email));
            return null;
        }

        return createAccount(username, email, "", password, fullname, Regex.URL_IMAGE_DEFAULT);
    }

    @Override
    public Account createAccountWithPhone(String phone, String password, String fullname) {
        if ((accountRepository.findByPhone(phone)).isPresent()) {
            logger.error(String.format(LogMessage.LOG_ACCOUNT_EXIST_PHONE, phone));
            return null;
        }

        String username = generateUsername(phone);
        if (username == null) {
            logger.error(String.format(LogMessage.LOG_INVALID_LOGIN_ID, phone));
            return null;
        }

        return createAccount(username, "", phone, password, fullname, Regex.URL_IMAGE_DEFAULT);
    }

//    @Override
//    public AgencyInfo registerAgency(String shopName, String shopAddress, String shopEmail, String shopPhone, String taxCode, String idCardNumber, String frontIdCardImageUrl, String backIdCardImageUrl, String professionalCertUrl, String businessLicenseUrl){
//        if(agencyInfoRepository.findByIdCardNumber(idCardNumber).isPresent()){
//            logger.error(String.format(LogMessage.LOG_AGENCY_EXIST_ID_NUMBER, idCardNumber));
//            return null;
//        }
//        return regiesterAgencyInfo(shopName, shopAddress, shopEmail, shopPhone, taxCode, idCardNumber, frontIdCardImageUrl, backIdCardImageUrl, professionalCertUrl, businessLicenseUrl);
//    }

    @Override
    public ApiResponse<AgencyInfoDTO> registerAgency(AgencyRegisterRequest request) {
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isPresent()) {
            if(account.get().getRole().getRoleCode().equals(RoleCode.ROLE_AGENCY)) {
                return ApiResponse.<AgencyInfoDTO>builder()
                        .status(ErrorCode.CONFLICT)
                        .message(Message.ACCOUNT_IS_AGENCY)
                        .timestamp(new java.util.Date())
                        .build();
            }

            Optional<AgencyInfo> existingAgency = agencyInfoRepository.findTopByAccountOrderBySubmittedDateDesc(account.get());
            if (existingAgency.isPresent()) {
                return ApiResponse.<AgencyInfoDTO>builder()
                        .status(ErrorCode.CONFLICT)
                        .message(Message.AGENCY_IS_PENDING)
                        .data(convertToAgencyInfoDTO(existingAgency.get()))
                        .timestamp(new java.util.Date())
                        .build();
            }

            try{
                AgencyRegisterDTO agencyRegisterDTO = request.getRegistrationData();

                AgencyInfo agencyInfo = new AgencyInfo();
                agencyInfo.setAccount(account.get());
                agencyInfo.setShopName(agencyRegisterDTO.getShop_name());
                agencyInfo.setShopEmail(agencyRegisterDTO.getShop_email());
                agencyInfo.setShopPhone(agencyRegisterDTO.getShop_phone());
                agencyInfo.setShopAddressDetail(agencyRegisterDTO.getShop_address());
                agencyInfo.setTaxNumber(agencyRegisterDTO.getTax_code());

                agencyInfo.setFullNameApplicant(agencyRegisterDTO.getFull_name_applicant());
                agencyInfo.setBirthdateApplicant(agencyRegisterDTO.getBirth_date_applicant());
                agencyInfo.setGenderApplicant(agencyRegisterDTO.getGender_applicant());
                agencyInfo.setIdCardNumber(agencyRegisterDTO.getId_card_number_applicant());
                agencyInfo.setDateOfIssueIdCard(agencyRegisterDTO.getDate_of_issue_card());
                agencyInfo.setPlaceOfIssueIdCard(agencyRegisterDTO.getPlace_of_issue_card());

                agencyInfo.setIdCardFrontImageUrl(request.getIdCardFrontUrl());
                agencyInfo.setIdCardBackImageUrl(request.getIdCardBackUrl());
                agencyInfo.setBusinessLicenseUrls(request.getBusinessLicenseUrl());
                agencyInfo.setProfessionalCertUrls(request.getProfessionalCertificateUrl());
                agencyInfo.setDiplomaCertUrls(request.getDiplomaCertificateUrl());

                agencyInfo.setApprovalStatus(approvalStatusRepository.findApprovalStatusByStatusCode(StatusCode.STATUS_PENDING));
                agencyInfo.setSubmittedDate(new java.util.Date());
                AgencyInfo savedAgencyInfo = agencyInfoRepository.save(agencyInfo);

                AgencyInfoDTO agencyInfoDTO = convertToAgencyInfoDTO(savedAgencyInfo);

                return ApiResponse.<AgencyInfoDTO>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.AGENCY_REGISTER_SUCCESS)
                        .data(agencyInfoDTO)
                        .timestamp(new java.util.Date())
                        .build();

            }catch (Exception e){
                logger.error(String.format(LogMessage.LOG_AGENCY_REGISTRATION_FAILED, accountRepository.findByAccountId(account.get().getAccountId()).getAccountId(), e.getMessage()));
            }
        }
        return ApiResponse.<AgencyInfoDTO>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Async
    @Override
    public void updateAccount(Account account) {
        accountRepository.save(account);
    }

    @Override
    public ApiResponse<AccountInfoDTO> getCurrentUserInfo() {
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isPresent()) {
            AccountInfoDTO accountInfoDTO = getAccountInfoDTO(account.get());
            if (accountInfoDTO != null) {
                return ApiResponse.<AccountInfoDTO>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.GET_INFO_SUCCESS)
                        .data(accountInfoDTO)
                        .timestamp(new java.util.Date())
                        .build();
            }
        }
        return ApiResponse.<AccountInfoDTO>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<AccountInfoDTO> updateProfile(UpdateAccountRequest request) {
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isPresent()) {
            account.get().setFullname(request.getFullname());
            account.get().setGender(request.getGender());
            account.get().setBirthdate(request.getBirthday());
            account.get().setPhone(request.getPhone());
            updateAccount(account.get());
            AccountInfoDTO accountInfoDTO = getAccountInfoDTO(account.get());
            if (accountInfoDTO != null) {
                return ApiResponse.<AccountInfoDTO>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.GET_INFO_SUCCESS)
                        .data(accountInfoDTO)
                        .timestamp(new java.util.Date())
                        .build();
            }
        }
        return ApiResponse.<AccountInfoDTO>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<AccountInfoDTO> addLoginId(AddLoginIdRequest request) {
        Optional<Account> existAccountContainLoginId = findAccountByLoginId(request.getLoginId());
        if (existAccountContainLoginId.isEmpty()) {
            Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
            if (account.isPresent()) {
                if (Regex.isValidEmail(request.getLoginId())) {
                    account.get().setEmail(request.getLoginId());
                    updateAccount(account.get());
                    AccountInfoDTO accountInfoDTO = getAccountInfoDTO(account.get());
                    if (accountInfoDTO != null) {
                        return ApiResponse.<AccountInfoDTO>builder()
                                .status(ErrorCode.SUCCESS)
                                .message(Message.GET_INFO_SUCCESS)
                                .data(accountInfoDTO)
                                .timestamp(new java.util.Date())
                                .build();
                    }
                } else if (Regex.isValidPhoneNumber(request.getLoginId())) {
                    account.get().setPhone(request.getLoginId());
                    updateAccount(account.get());
                    AccountInfoDTO accountInfoDTO = getAccountInfoDTO(account.get());
                    if (accountInfoDTO != null) {
                        return ApiResponse.<AccountInfoDTO>builder()
                                .status(ErrorCode.SUCCESS)
                                .message(Message.GET_INFO_SUCCESS)
                                .data(accountInfoDTO)
                                .timestamp(new java.util.Date())
                                .build();
                    }
                }
                return ApiResponse.<AccountInfoDTO>builder()
                        .status(ErrorCode.BAD_REQUEST)
                        .message(Message.LOGIN_ID_IN_VALID)
                        .timestamp(new java.util.Date())
                        .build();
            }
            return ApiResponse.<AccountInfoDTO>builder()
                    .status(ErrorCode.FORBIDDEN)
                    .message(Message.ACCOUNT_NOT_FOUND)
                    .timestamp(new java.util.Date())
                    .build();
        }
        return ApiResponse.<AccountInfoDTO>builder()
                .status(ErrorCode.ACCOUNT_ALREADY_EXISTS)
                .message(Message.LOGIN_ID_EXIST)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<AccountInfoDTO> updateAvatar(UpdateAvatarRequest request) {
        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (account.isPresent()) {
            Optional<Multimedia> multimedia = multimediaRepository.findByAccount(account.get());
            if (multimedia.isPresent()) {
                multimedia.get().setMultimediaUrl(request.getAvatar_url());
                multimediaRepository.save(multimedia.get());
                AccountInfoDTO accountInfoDTO = getAccountInfoDTO(account.get());
                if (accountInfoDTO != null) {
                    return ApiResponse.<AccountInfoDTO>builder()
                            .status(ErrorCode.SUCCESS)
                            .message(Message.GET_INFO_SUCCESS)
                            .data(accountInfoDTO)
                            .timestamp(new java.util.Date())
                            .build();
                }
            }
        }
        return ApiResponse.<AccountInfoDTO>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<String> changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<Account> currentUser = accountRepository.findByUsername(authentication.getName());
        if (currentUser.isPresent()) {
            if (passwordEncoder.matches(request.getOld_password(), currentUser.get().getPassword())) {
                currentUser.get().setPassword(passwordEncoder.encode(request.getNew_password()));
                updateAccount(currentUser.get());
                logger.info(String.format(LogMessage.LOG_CHANGE_PASSWORD_DONE, authentication.getName()));
                return ApiResponse.<String>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.CHANGE_PASSWORD_SUCCESS)
                        .timestamp(new java.util.Date())
                        .build();
            } else {
                logger.info(String.format(LogMessage.LOG_INCORRECT_CURRENT_PASSWORD, authentication.getName()));
                return ApiResponse.<String>builder()
                        .status(ErrorCode.BAD_REQUEST)
                        .message(Message.CURRENT_PASSWORD_NOT_CORRECT)
                        .timestamp(new java.util.Date())
                        .build();

            }
        }
        return null;
    }

    private Account createAccount(String username, String email, String phone, String password, String fullname, String imageLink) {
        Account savedAccount = accountRepository.save(Account.builder()
                .username(username)
                .email(email)
                .phone(phone)
                .fullname(fullname)
                .birthdate(Date.valueOf(Regex.BIRTHDATE_DEFAULT))
                .gender(Regex.GENDER_MALE)
                .password(passwordEncoder.encode(password))
                .isBanned(false)
                .role(roleRepository.findByRoleCode(RoleCode.ROLE_CUSTOMER))
                .accountStatus(Account.AccountStatus.ACTIVE)
                .build());
        multimediaRepository.save(Multimedia.builder().multimediaUrl(imageLink).account(savedAccount).multimediaType(MultimediaType.IMAGE).build());
        cartRepository.save(Cart.builder().account(savedAccount).totalItem(0).build());
        return savedAccount;
    }

//    private AgencyInfo regiesterAgencyInfo(String shopName, String shopAddress, String shopEmail, String shopPhone, String taxCode, String idCardNumber, String frontIdCardImageUrl, String backIdCardImageUrl, String professionalCertUrl, String businessLicenseUrl) {
//        Optional<Account> account = accountRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
//        AgencyInfo saveAgencyInfo = agencyInfoRepository.save(AgencyInfo.builder()
//                .account(account.get())
//                .shopName(shopName)
//                .shopEmail(shopEmail)
//                .shopPhone(shopPhone)
//                .shopAddressDetail(shopAddress)
//                .taxNumber(taxCode)
//                .idCardNumber(idCardNumber)
//                .idCardBackImageUrl(backIdCardImageUrl)
//                .idCardFrontImageUrl(frontIdCardImageUrl)
//                .businessLicenseUrls(businessLicenseUrl)
//                .professionalCertUrls(professionalCertUrl)
//                .genderApplicant(account.get().getGender())
//                .submittedDate(new java.util.Date())
//                .approvalStatus(approvalStatusRepository.findApprovalStatusByStatusCode(StatusCode.STATUS_PENDING))
//                .build());
//        return saveAgencyInfo;
//    }

    private String generateUsername(String loginId) {
        if (Regex.isValidEmail(loginId)) {
            int atIndex = loginId.indexOf('@');
            if (atIndex != -1) {
                return loginId.substring(0, atIndex);
            }
        } else if (Regex.isValidPhoneNumber(loginId)) {
            String numericPhoneNumber = loginId.replaceAll("[^0-9]", "");
            int length = numericPhoneNumber.length();
            if (length >= 3) {
                String lastThreeDigits = numericPhoneNumber.substring(length - 3);
                return "user" + lastThreeDigits;
            }
        }
        return null;
    }

    private AccountInfoDTO getAccountInfoDTO(Account account) {
        Optional<Multimedia> multimedia = multimediaRepository.findByAccount(account);
        Optional<Cart> cart = cartRepository.findByAccount(account);
        if (multimedia.isPresent() && cart.isPresent()) {
            return changeToAccountDTO(account, cart.get(), multimedia.get());
        }
        return null;
    }

    private AccountInfoDTO changeToAccountDTO(Account account, Cart cart, Multimedia multimedia) {
        return new AccountInfoDTO(
                account.getAccountId(),
                account.getUsername(),
                multimedia.getMultimediaUrl(),
                account.getRole().getRoleCode(),
                new CartDTO(cart.getCartId(), cart.getTotalItem(), getCartItemList(cart.getCartId())),
                new AccountProfileDTO(account.getFullname(), account.getEmail(), account.getPhone(), account.getGender(), account.getBirthdate())
        );
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

    public Optional<Account> findAccountByLoginId(String loginId) {
        if (Regex.isValidEmail(loginId)) {
            return accountRepository.findByEmail(loginId);
        } else if (Regex.isValidPhoneNumber(loginId)) {
            return accountRepository.findByPhone(loginId);
        } else {
            return accountRepository.findByUsername(loginId);
        }
    }

    @Override
    public Optional<Account> findCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return accountRepository.findByUsername(authentication.getName());
    }

    public List<CartItemDTO> getCartItemList(Long cartId) {
        List<CartItem> cartList = cartItemRepository.findAllByCart_CartId(cartId);

        return cartList.stream()
                .map(cartItem -> {
                    ProductInfoDTO productInfoDTO = productService.getProductInformation(cartItem.getProduct());
                    Optional<ProductVariantDTO> matchingVariant = productInfoDTO.getProduct_variant_list()
                            .stream()
                            .filter(variant -> variant.getProduct_variant_id().equals(cartItem.getProductVariant().getProductVariantId()))
                            .findFirst();
                    return new CartItemDTO(
                            cartItem.getCartItemId(),
                            productService.getProductInformation(cartItem.getProduct()),
                            matchingVariant.get(),
                            cartItem.getQuantity(),
                            calculateSubTotal(cartItem)
                    );
                })
                .collect(Collectors.toList());
    }

    private String calculateSubTotal(CartItem cartItem) {
        BigDecimal price;
        if (cartItem.getProduct() != null) {
            price = cartItem.getProduct().getSalePrice();
        } else if (cartItem.getProductVariant() != null) {
            price = cartItem.getProductVariant().getSalePrice();
        } else {
            return "0.00";
        }
        BigDecimal subTotal = price.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        return Regex.formatPriceToVND(subTotal);
    }


}
