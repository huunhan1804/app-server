package com.example.shoppingsystem.api;

import com.example.shoppingsystem.auth.interfaces.AuthenticationService;
import com.example.shoppingsystem.config.HttpClientWrapper;
import com.example.shoppingsystem.constants.LogMessage;
import com.example.shoppingsystem.requests.*;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.responses.AuthenticationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final HttpClientWrapper httpClientWrapper;
    private static final Logger logger = LogManager.getLogger(AuthenticationController.class);

    @Operation(summary = "API for authenticate", description = "This API will check authenticate base on email and password.")
    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(@RequestBody AuthenticationRequest request) {
        logger.info(String.format(LogMessage.LOG_RECEIVED_AUTHENTICATE_REQUEST, request.getLoginId()));
        ApiResponse<AuthenticationResponse> apiResponse = authenticationService.authenticate(request);
        logger.info(String.format(LogMessage.LOG_AUTHENTICATION_SUCCESSFUL, request.getLoginId()));
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API for authenticate with google", description = "This API will automatic authenticate if account is already exists, then will register this account with a default password sent to email.")
    @PostMapping("/authenticate-google")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticateGG(@RequestBody AuthenticationGoogleRequest request) {
        logger.info(String.format(LogMessage.LOG_RECEIVED_AUTHENTICATE_GOOGLE_REQUEST, request.getToken()));
        ApiResponse<AuthenticationResponse> apiResponse = authenticationService.authenticateWithGoogle(request, httpClientWrapper);
        logger.info(String.format(LogMessage.LOG_GOOGLE_AUTHENTICATION_SUCCESSFUL, request.getToken()));
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);

    }

    @Operation(summary = "API for send OTP to register function", description = "This API will send a OTP to email, and use this otp to API register")
    @PostMapping("/send-otp-registration")
    public ResponseEntity<ApiResponse<String>> sendOTP(@RequestBody SendOTPRequest request) {
        logger.info(String.format(LogMessage.LOG_RECEIVED_SEND_OTP_REQUEST, request.getLoginId()));
        ApiResponse<String> apiResponse = authenticationService.sendOtpRegistration(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API for register", description = "This API will register a new account base on OTP into database with email and password.")
    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> registration(@RequestBody RegistrationRequest request) {
        logger.info(String.format(LogMessage.LOG_RECEIVED_REGISTER_REQUEST, request.getLoginId()));
        ApiResponse<AuthenticationResponse> apiResponse = authenticationService.registration(request);
        logger.info(String.format(LogMessage.LOG_REGISTRATION_SUCCESSFUL, request.getLoginId()));
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API for register", description = "This API will register a new account base on OTP into database with email and password.")
    @PostMapping("/registration-with-phone")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> registrationWithPhone(@RequestBody RegistrationRequest request) {
        logger.info(String.format(LogMessage.LOG_RECEIVED_REGISTER_REQUEST, request.getLoginId()));
        ApiResponse<AuthenticationResponse> apiResponse = authenticationService.registrationWithPhone(request);
        logger.info(String.format(LogMessage.LOG_REGISTRATION_SUCCESSFUL, request.getLoginId()));
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API for send OTP to forgot password function", description = "This API will send a OTP to email, and use this otp to API forgot password")
    @PostMapping("/send-otp-forgot-password")
    public ResponseEntity<ApiResponse<String>> sendOTPForgotPassword(@RequestBody SendOTPRequest request) {
        logger.info(String.format(LogMessage.LOG_RECEIVED_SEND_OTP_FORGOT_PASSWORD_REQUEST, request.getLoginId()));
        ApiResponse<String> apiResponse = authenticationService.sendOtpForgotPassword(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API for forgot password", description = "This API will set a new password for account based on OTP sent to email.")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        logger.info(LogMessage.LOG_RECEIVED_FORGOT_PASSWORD_REQUEST);
        ApiResponse<String> apiResponse = authenticationService.forgotPassword(request);
        logger.info(String.format(LogMessage.LOG_SEND_OTP_FORGOT_PASSWORD_SUCCESS, request.getLoginId()));
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);

    }

    @Operation(summary = "API for refreshing access token", description = "This API will refresh the access token using the refresh token")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        logger.info(LogMessage.LOG_RECEIVED_REFRESH_TOKEN_REQUEST);
        ApiResponse<AuthenticationResponse> apiResponse = authenticationService.getAccessTokenFromRefreshToken(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }


}
