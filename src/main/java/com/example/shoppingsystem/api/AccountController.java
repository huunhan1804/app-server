package com.example.shoppingsystem.api;

import com.example.shoppingsystem.constants.LogMessage;
import com.example.shoppingsystem.dtos.AccountInfoDTO;
import com.example.shoppingsystem.requests.AddLoginIdRequest;
import com.example.shoppingsystem.requests.ChangePasswordRequest;
import com.example.shoppingsystem.requests.UpdateAccountRequest;
import com.example.shoppingsystem.requests.UpdateAvatarRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Tag(name = "Account")
public class AccountController {
    private static final Logger logger = LogManager.getLogger(AccountController.class);
    private final AccountService accountService;

    //Authenticated
    @Operation(summary = "API for get currentUser", description = "This API will return  a json with information basic of account base on Token.")
    @GetMapping("/current-user")
    @ResponseBody
    public ResponseEntity<ApiResponse<AccountInfoDTO>> getCurrentUser() {
        logger.info(LogMessage.LOG_RECEIVED_CURRENT_ACCOUNT);
        ApiResponse<AccountInfoDTO> apiResponse = accountService.getCurrentUserInfo();
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API Update account", description = "This API will edit Account information")
    @PostMapping("/update-user")
    public ResponseEntity<ApiResponse<AccountInfoDTO>> updateAccountProfile(@RequestBody UpdateAccountRequest request) {
        ApiResponse<AccountInfoDTO> apiResponse = accountService.updateProfile(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API add loginId", description = "This API will add email or phone for this account")
    @PostMapping("/add-login-id")
    public ResponseEntity<ApiResponse<AccountInfoDTO>> addLoginId(@RequestBody AddLoginIdRequest request) {
        ApiResponse<AccountInfoDTO> apiResponse = accountService.addLoginId(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API Update avatar account", description = "This API will update a new avatar for this account")
    @PostMapping("/update-avatar-user")
    public ResponseEntity<ApiResponse<AccountInfoDTO>> updateAvatar(@RequestBody UpdateAvatarRequest request) {
        ApiResponse<AccountInfoDTO> apiResponse = accountService.updateAvatar(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    //Authenticated
    @Operation(summary = "API for change password", description = "This API will change password of account.")
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody ChangePasswordRequest request) {
        logger.info(LogMessage.LOG_RECEIVED_CHANGE_PASSWORD_REQUEST);
        ApiResponse<String> apiResponse = accountService.changePassword(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

}
