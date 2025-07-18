package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.AgencyInfo;
import com.example.shoppingsystem.requests.UpdateAccountRequest;
import com.example.shoppingsystem.requests.AddLoginIdRequest;
import com.example.shoppingsystem.requests.UpdateAvatarRequest;
import com.example.shoppingsystem.requests.ChangePasswordRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.dtos.AccountInfoDTO;
import org.springframework.scheduling.annotation.Async;

import java.util.Optional;

public interface AccountService {

    Optional<Account> findAccountByLoginId(String loginId);

    Optional<Account> findCurrentUserInfo();

    Account createAccountWithSocialLogin(String email, String password, String fullname, String imageLink);

    Account createAccountWithEmail(String email, String password, String fullname);

    Account createAccountWithPhone(String phone, String password, String fullname);

    AgencyInfo registerAgency(String shopName, String shopAddress, String shopEmail, String shopPhone,
                              String taxCode, String idCardNumber, String frontIdCardImageUrl,
                              String backIdCardImageUrl, String professionalCertUrl, String businessLicenseUrl);

    @Async
    void updateAccount(Account account);

    ApiResponse<AccountInfoDTO> getCurrentUserInfo();

    ApiResponse<AccountInfoDTO> updateProfile(UpdateAccountRequest request);

    ApiResponse<AccountInfoDTO> addLoginId(AddLoginIdRequest request);

    ApiResponse<AccountInfoDTO> updateAvatar(UpdateAvatarRequest request);

    ApiResponse<String> changePassword(ChangePasswordRequest request);
}