package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.*;
import com.example.shoppingsystem.dtos.*;
import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.Cart;
import com.example.shoppingsystem.entities.CartItem;
import com.example.shoppingsystem.entities.Multimedia;
import com.example.shoppingsystem.enums.MultimediaType;
import com.example.shoppingsystem.repositories.*;
import com.example.shoppingsystem.requests.AddLoginIdRequest;
import com.example.shoppingsystem.requests.ChangePasswordRequest;
import com.example.shoppingsystem.requests.UpdateAccountRequest;
import com.example.shoppingsystem.requests.UpdateAvatarRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.AccountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
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

    @Autowired
    public AccountServiceImpl(PasswordEncoder passwordEncoder, AccountRepository accountRepository, RoleRepository roleRepository, MultimediaRepository multimediaRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, ProductServiceImpl productService) {
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.multimediaRepository = multimediaRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
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
                .build());
        multimediaRepository.save(Multimedia.builder().multimediaUrl(imageLink).account(savedAccount).multimediaType(MultimediaType.IMAGE).build());
        cartRepository.save(Cart.builder().account(savedAccount).totalItem(0).build());
        return savedAccount;
    }

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
