package com.example.shoppingsystem.auth.interfaces;

import com.example.shoppingsystem.config.HttpClientWrapper;
import com.example.shoppingsystem.requests.*;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.responses.AuthenticationResponse;
import org.springframework.stereotype.Service;

@Service
public interface AuthenticationService {
    ApiResponse<AuthenticationResponse> authenticate(AuthenticationRequest request);
    ApiResponse<AuthenticationResponse> authenticateWithGoogle(AuthenticationGoogleRequest request, HttpClientWrapper clientWrapper);
    ApiResponse<AuthenticationResponse> registration(RegistrationRequest request);
    ApiResponse<String> sendOtpRegistration(SendOTPRequest sendOTPRequest);
    ApiResponse<String> sendOtpForgotPassword(SendOTPRequest sendOTPRequest);
    ApiResponse<String> forgotPassword(ForgotPasswordRequest request);
    ApiResponse<AuthenticationResponse> getAccessTokenFromRefreshToken(RefreshTokenRequest request);

    ApiResponse<AuthenticationResponse> registrationWithPhone(RegistrationRequest request);
}
