package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.AccountInfoDTO;
import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.requests.*;
import com.example.shoppingsystem.responses.ApiResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface AccountService {
    Optional<Account> findAccountByLoginId(String loginId);
    Optional<Account> findCurrentUserInfo();
    Account createAccountWithSocialLogin(String email, String password, String fullname, String imageLink);
    Account createAccountWithEmail(String email, String password, String fullname);
    Account createAccountWithPhone(String phone, String password, String fullname);
    @Async
    void updateAccount(Account account);
    ApiResponse<AccountInfoDTO> getCurrentUserInfo();
    ApiResponse<AccountInfoDTO> updateProfile(UpdateAccountRequest request);
    ApiResponse<AccountInfoDTO> addLoginId(AddLoginIdRequest request);
    ApiResponse<AccountInfoDTO> updateAvatar(UpdateAvatarRequest request);
    ApiResponse<String> changePassword(ChangePasswordRequest request);
}
