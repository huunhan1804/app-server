package com.example.shoppingsystem.auth;

import com.example.shoppingsystem.auth.interfaces.*;
import com.example.shoppingsystem.config.HttpClientWrapper;
import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.LogMessage;
import com.example.shoppingsystem.constants.Message;
import com.example.shoppingsystem.constants.Regex;
import com.example.shoppingsystem.entities.AccessToken;
import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.Otp;
import com.example.shoppingsystem.entities.RefreshToken;
import com.example.shoppingsystem.enums.OtpPurpose;
import com.example.shoppingsystem.extensions.PasswordUtil;
import com.example.shoppingsystem.repositories.AccountRepository;
import com.example.shoppingsystem.requests.*;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.responses.AuthenticationResponse;
import com.example.shoppingsystem.services.interfaces.AccountService;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger logger = LogManager.getLogger(AccessTokenServiceImpl.class);

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpService otpService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final AccountService accountService;

    @Autowired
    public AuthenticationServiceImpl(AccountRepository accountRepository, PasswordEncoder passwordEncoder, EmailService emailService, OtpService otpService, AccessTokenService accessTokenService, RefreshTokenService refreshTokenService, AccountService accountService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.otpService = otpService;
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
        this.accountService = accountService;
    }


    @Override
    public ApiResponse<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        String loginId = request.getLoginId();
        logger.info(String.format(LogMessage.LOG_PROCESSING_AUTHENTICATE, loginId));

        Optional<Account> account = accountService.findAccountByLoginId(loginId);

        if (account.isEmpty()) {
            return ApiResponse.<AuthenticationResponse>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.ACCOUNT_NOT_FOUND)
                    .timestamp(new Date())
                    .build();
        }

        if (!passwordEncoder.matches(request.getPassword(), account.get().getPassword())) {
            logger.info(String.format(LogMessage.LOG_INCORRECT_CURRENT_PASSWORD, loginId));
            return ApiResponse.<AuthenticationResponse>builder()
                    .status(ErrorCode.UNAUTHORIZED)
                    .message(Message.INVALID_CREDENTIALS)
                    .timestamp(new Date())
                    .build();
        }

        if (account.get().isBanned()) {
            logger.info(String.format(LogMessage.LOG_ACCOUNT_BANNED, loginId));
            return ApiResponse.<AuthenticationResponse>builder()
                    .status(ErrorCode.FORBIDDEN)
                    .message(Message.ACCOUNT_BANNED)
                    .timestamp(new Date())
                    .build();
        }

        RefreshToken refreshToken = refreshTokenService.createToken(account.get());
        AccessToken accessToken = accessTokenService.createToken(account.get(), refreshToken);

        if (accessToken == null) {
            logger.info(String.format(LogMessage.LOG_TOO_MANY_REQUESTS_ACCESS_TOKEN, loginId));
            return ApiResponse.<AuthenticationResponse>builder()
                    .status(ErrorCode.FORBIDDEN)
                    .message(Message.TOO_MANY_REQUESTS_ACCESS_TOKEN)
                    .timestamp(new Date())
                    .build();
        } else {
            logger.info(String.format(LogMessage.LOG_AUTHENTICATE_DONE, loginId));
            return ApiResponse.<AuthenticationResponse>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.AUTHENTICATE_SUCCESS)
                    .data(new AuthenticationResponse(accessToken.getToken(), refreshToken.getToken()))
                    .timestamp(new Date())
                    .build();
        }
    }

    @Override
    public ApiResponse<AuthenticationResponse> authenticateWithGoogle(AuthenticationGoogleRequest request, HttpClientWrapper clientWrapper) {
        String token = request.getToken();
        logger.info(String.format(LogMessage.LOG_PROCESSING_AUTHENTICATE_GOOGLE, token));

        HttpClientBuilder.create().build();
        String url = Regex.BASE_URL_AUTHENTICATE_WITH_GOOGLE + token;

        HttpGet httpRequest = new HttpGet(url);

        try {
            HttpResponse httpResponse = clientWrapper.execute(httpRequest);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            logger.info(String.format(LogMessage.LOG_RECEIVE_FROM_RESPONSE_CODE, statusCode, token));

            if (statusCode == 200) {
                String responseJson = EntityUtils.toString(httpResponse.getEntity());
                JSONObject json = new JSONObject(responseJson);
                String email = json.getString("email");
                logger.info(String.format(LogMessage.LOG_EXTRACT_EMAIL_FROM_RESPONSE_GOOGLE, email, token));

                Optional<Account> account = accountRepository.findByEmail(email);
                if (account.isPresent()) {
                    logger.info(String.format(LogMessage.LOG_FOUND_ACCOUNT, email));

                    if (account.get().isBanned()) {
                        logger.info(String.format(LogMessage.LOG_ACCOUNT_BANNED, email));
                        return ApiResponse.<AuthenticationResponse>builder()
                                .status(ErrorCode.FORBIDDEN)
                                .message(Message.ACCOUNT_BANNED)
                                .timestamp(new Date())
                                .build();
                    } else {
                        RefreshToken refreshToken = refreshTokenService.createToken(account.get());
                        AccessToken accessToken = accessTokenService.createToken(account.get(), refreshToken);
                        if (accessToken == null) {
                            logger.info(String.format(LogMessage.LOG_TOO_MANY_REQUESTS_ACCESS_TOKEN, email));
                            return ApiResponse.<AuthenticationResponse>builder()
                                    .status(ErrorCode.TOO_MANY_REQUEST)
                                    .message(Message.TOO_MANY_REQUESTS_ACCESS_TOKEN)
                                    .timestamp(new Date())
                                    .build();
                        } else {
                            logger.info(String.format(LogMessage.LOG_AUTHENTICATE_GOOGLE_DONE, email));
                            return ApiResponse.<AuthenticationResponse>builder()
                                    .status(ErrorCode.SUCCESS)
                                    .message(Message.LOGIN_GG_SUCCESS)
                                    .data(new AuthenticationResponse(accessToken.getToken(), refreshToken.getToken()))
                                    .timestamp(new Date())
                                    .build();
                        }
                    }
                } else {
                    logger.info(String.format(LogMessage.LOG_ACCOUNT_NOT_FOUND_EMAIL, email));
                    logger.info(String.format(LogMessage.LOG_PROCESSING_REGISTER, email));
                    String password = PasswordUtil.generateRandomPassword();
                    emailService.sendPasswordEmail(email, password);
                    Account newAccount = accountService.createAccountWithSocialLogin(email, password, json.getString("name"), json.getString("picture"));
                    RefreshToken refreshToken = refreshTokenService.createToken(newAccount);
                    AccessToken accessToken = accessTokenService.createToken(newAccount, refreshToken);
                    if (accessToken == null) {
                        logger.info(String.format(LogMessage.LOG_TOO_MANY_REQUESTS_ACCESS_TOKEN, email));
                        return ApiResponse.<AuthenticationResponse>builder()
                                .status(ErrorCode.TOO_MANY_REQUEST)
                                .message(Message.TOO_MANY_REQUESTS_ACCESS_TOKEN)
                                .timestamp(new Date())
                                .build();
                    } else {
                        logger.info(String.format(LogMessage.LOG_REGISTER_DONE, email));
                        return ApiResponse.<AuthenticationResponse>builder()
                                .status(ErrorCode.SUCCESS)
                                .message(Message.REGISTER_GG_SUCCESS)
                                .timestamp(new Date())
                                .data(new AuthenticationResponse(accessToken.getToken(), refreshToken.getToken()))
                                .build();
                    }
                }
            } else {
                logger.info(String.format(LogMessage.LOG_INVALID_GOOGLE_TOKEN, token, statusCode));
                return ApiResponse.<AuthenticationResponse>builder()
                        .status(ErrorCode.FORBIDDEN)
                        .message(String.format(Message.INVALID_GOOGLE_TOKEN, statusCode))
                        .timestamp(new Date())
                        .build();
            }
        } catch (java.io.IOException | JSONException e) {
            logger.error(String.format(LogMessage.LOG_ERROR_TO_AUTHENTICATE_GG, token, e.getMessage()));
            return ApiResponse.<AuthenticationResponse>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message(Message.ERROR_TO_AUTHENTICATE_GG + " : " + e.getMessage())
                    .timestamp(new Date())
                    .build();
        } finally {
            httpRequest.releaseConnection();
        }
    }

    @Override
    public ApiResponse<AuthenticationResponse> registration(RegistrationRequest request) {
        String loginId = request.getLoginId();
        logger.info(String.format(LogMessage.LOG_PROCESSING_REGISTER, loginId));

        Optional<Account> existingAccount = accountService.findAccountByLoginId(loginId);

        if (existingAccount.isPresent()) {
            logger.info(String.format(LogMessage.LOG_ACCOUNT_EXISTS, loginId));
            return ApiResponse.<AuthenticationResponse>builder()
                    .status(ErrorCode.ACCOUNT_ALREADY_EXISTS)
                    .message(Message.ACCOUNT_ALREADY_EXISTS)
                    .timestamp(new Date())
                    .build();
        }

        if (!otpService.isOtpValid(request.getOtp_code(), loginId, OtpPurpose.REGISTRATION)) {
            return ApiResponse.<AuthenticationResponse>builder()
                    .status(ErrorCode.BAD_REQUEST)
                    .message(Message.INVALID_OTP)
                    .timestamp(new Date())
                    .build();
        }

        Account newAccount = accountService.createAccountWithEmail(loginId, request.getPassword(), request.getFullname());

        if (newAccount != null) {
            RefreshToken refreshToken = refreshTokenService.createToken(newAccount);
            AccessToken accessToken = accessTokenService.createToken(newAccount, refreshToken);

            if (accessToken != null) {
                logger.info(String.format(LogMessage.LOG_REGISTER_SUCCESS, newAccount.getEmail()));
                Optional<Otp> otp = otpService.getOtpByLoginIdAndPurpose(loginId, OtpPurpose.REGISTRATION);
                otp.ifPresent(otpService::deleteOtp);
                return ApiResponse.<AuthenticationResponse>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.REGISTRATION_SUCCESS)
                        .data(new AuthenticationResponse(accessToken.getToken(), refreshToken.getToken()))
                        .timestamp(new Date())
                        .build();
            }
        }

        return ApiResponse.<AuthenticationResponse>builder()
                .status(ErrorCode.INTERNAL_SERVER_ERROR)
                .message(Message.REGISTRATION_FAILURE)
                .timestamp(new Date())
                .build();
    }

    @Override
    public ApiResponse<String> sendOtpRegistration(SendOTPRequest sendOTPRequest) {
        Optional<Account> account = accountService.findAccountByLoginId(sendOTPRequest.getLoginId());
        if (account.isEmpty()) {
            if (Regex.isValidEmail(sendOTPRequest.getLoginId())) {
                Otp otp = otpService.createOtp(sendOTPRequest.getLoginId(), OtpPurpose.REGISTRATION);
                emailService.sendOTP(sendOTPRequest.getLoginId(), otp.getOtpCode(), OtpPurpose.REGISTRATION);
                return ApiResponse.<String>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.SEND_OTP_SUCCESS)
                        .timestamp(new Date())
                        .build();
            }
        }
        return ApiResponse.<String>builder()
                .status(ErrorCode.ACCOUNT_ALREADY_EXISTS)
                .message(Message.ACCOUNT_ALREADY_EXISTS)
                .timestamp(new Date())
                .build();
    }

    @Override
    public ApiResponse<String> sendOtpForgotPassword(SendOTPRequest sendOTPRequest) {
        Optional<Account> account = accountService.findAccountByLoginId(sendOTPRequest.getLoginId());
        if (account.isPresent()) {
            if (Regex.isValidEmail(sendOTPRequest.getLoginId())) {
                Otp otp = otpService.createOtp(sendOTPRequest.getLoginId(), OtpPurpose.PASSWORD_RESET);
                emailService.sendOTP(sendOTPRequest.getLoginId(), otp.getOtpCode(), OtpPurpose.PASSWORD_RESET);
                return ApiResponse.<String>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.SEND_OTP_SUCCESS)
                        .timestamp(new Date())
                        .build();
            }
        }
        return ApiResponse.<String>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new Date())
                .build();
    }

    @Override
    public ApiResponse<String> forgotPassword(ForgotPasswordRequest request) {
        String loginId = request.getLoginId();
        logger.info(String.format(LogMessage.LOG_PROCESSING_REGISTER, loginId));

        Optional<Account> existingAccount = accountService.findAccountByLoginId(loginId);

        if (existingAccount.isEmpty()) {
            logger.info(String.format(LogMessage.LOG_ACCOUNT_NOT_FOUND_EMAIL, loginId));
            return ApiResponse.<String>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.ACCOUNT_NOT_FOUND)
                    .timestamp(new Date())
                    .build();
        }

        if (!otpService.isOtpValid(request.getOtp_code(), loginId, OtpPurpose.PASSWORD_RESET)) {
            return ApiResponse.<String>builder()
                    .status(ErrorCode.BAD_REQUEST)
                    .message(Message.INVALID_OTP)
                    .timestamp(new Date())
                    .build();
        }

        existingAccount.get().setPassword(passwordEncoder.encode(request.getNew_password()));
        accountService.updateAccount(existingAccount.get());
        Optional<Otp> otp = otpService.getOtpByLoginIdAndPurpose(loginId, OtpPurpose.REGISTRATION);
        otp.ifPresent(otpService::deleteOtp);

        return ApiResponse.<String>builder()
                .status(ErrorCode.SUCCESS)
                .message(Message.CHANGE_PASSWORD_SUCCESS)
                .timestamp(new Date())
                .build();
    }


    @Override
    public ApiResponse<AuthenticationResponse> getAccessTokenFromRefreshToken(RefreshTokenRequest request) {
        logger.info(String.format(LogMessage.LOG_PROCESSING_NEW_ACCESS_TOKEN, request.getRefresh_token()));
        Optional<RefreshToken> refreshTokenOptional = refreshTokenService.findByToken(request.getRefresh_token());
        if (refreshTokenOptional.isPresent()) {
            RefreshToken refreshToken = refreshTokenOptional.get();
            if (refreshTokenService.verifyExpiration(refreshToken)) {
                Account userInfo = refreshToken.getAccount();
                AccessToken accessToken = accessTokenService.createToken(userInfo, refreshToken);
                if (accessToken != null) {
                    logger.info(String.format(LogMessage.LOG_NEW_ACCESS_TOKEN_CREATED, request.getRefresh_token()));
                    return ApiResponse.<AuthenticationResponse>builder()
                            .status(ErrorCode.SUCCESS)
                            .message(Message.NEW_ACCESS_TOKEN_CREATED)
                            .data(new AuthenticationResponse(accessToken.getToken(), request.getRefresh_token()))
                            .timestamp(new Date())
                            .build();
                } else {
                    logger.info(String.format(LogMessage.LOG_TOO_MANY_REQUESTS_ACCESS_TOKEN, request.getRefresh_token()));
                    return ApiResponse.<AuthenticationResponse>builder()
                            .status(ErrorCode.TOO_MANY_REQUEST)
                            .message(Message.TOO_MANY_REQUESTS_ACCESS_TOKEN)
                            .timestamp(new Date())
                            .build();
                }
            } else {
                // Perform necessary actions when the refreshToken is expired
                logger.info(String.format(LogMessage.LOG_REFRESH_TOKEN_EXPIRED, request.getRefresh_token()));
                return ApiResponse.<AuthenticationResponse>builder()
                        .status(ErrorCode.UNAUTHORIZED)
                        .message(Message.REFRESH_TOKEN_EXPIRED)
                        .timestamp(new Date())
                        .build();
            }
        } else {
            // Perform necessary actions when the refreshToken is not found in the database
            logger.info(String.format(LogMessage.LOG_REFRESH_TOKEN_INVALID, request.getRefresh_token()));
            return ApiResponse.<AuthenticationResponse>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message(Message.REFRESH_TOKEN_INVALID)
                    .timestamp(new Date())
                    .build();
        }
    }

    @Override
    public ApiResponse<AuthenticationResponse> registrationWithPhone(RegistrationRequest request) {
        String loginId = request.getLoginId();
        logger.info(String.format(LogMessage.LOG_PROCESSING_REGISTER, loginId));

        Optional<Account> existingAccount = accountService.findAccountByLoginId(loginId);

        if (existingAccount.isPresent()) {
            logger.info(String.format(LogMessage.LOG_ACCOUNT_EXISTS, loginId));
            return ApiResponse.<AuthenticationResponse>builder()
                    .status(ErrorCode.ACCOUNT_ALREADY_EXISTS)
                    .message(Message.ACCOUNT_ALREADY_EXISTS)
                    .timestamp(new Date())
                    .build();
        }

        Account newAccount = accountService.createAccountWithPhone(loginId, request.getPassword(), request.getFullname());

        if (newAccount != null) {
            RefreshToken refreshToken = refreshTokenService.createToken(newAccount);
            AccessToken accessToken = accessTokenService.createToken(newAccount, refreshToken);

            if (accessToken != null) {
                logger.info(String.format(LogMessage.LOG_REGISTER_SUCCESS, newAccount.getPhone()));
                return ApiResponse.<AuthenticationResponse>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.REGISTRATION_SUCCESS)
                        .data(new AuthenticationResponse(accessToken.getToken(), refreshToken.getToken()))
                        .timestamp(new Date())
                        .build();
            }
        }

        return ApiResponse.<AuthenticationResponse>builder()
                .status(ErrorCode.INTERNAL_SERVER_ERROR)
                .message(Message.REGISTRATION_FAILURE)
                .timestamp(new Date())
                .build();
    }
}
