package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.Message;
import com.example.shoppingsystem.dtos.AddressInfoDTO;
import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.Address;
import com.example.shoppingsystem.repositories.AddressRepository;
import com.example.shoppingsystem.requests.AddAddressRequest;
import com.example.shoppingsystem.requests.UpdateAddressRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.AccountService;
import com.example.shoppingsystem.services.interfaces.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final AccountService accountService;

    @Autowired
    public AddressServiceImpl(AddressRepository addressRepository, AccountService accountService) {
        this.addressRepository = addressRepository;
        this.accountService = accountService;
    }

    @Override
    public ApiResponse<List<AddressInfoDTO>> addAddress(AddAddressRequest request) {
        Optional<Account> account = accountService.findCurrentUserInfo();
        if (account.isPresent()) {
            Address address = new Address();
            address.setAccount(account.get());
            address.setFullName(request.getFullname());
            address.setPhone(request.getPhone());
            address.setAddressDetail(request.getAddress_detail());
            address.setIsDefault(false);
            addressRepository.save(address);
            return getAllAddress();
        }
        return ApiResponse.<List<AddressInfoDTO>>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<List<AddressInfoDTO>> getAllAddress() {
        Optional<Account> account = accountService.findCurrentUserInfo();
        if (account.isPresent()) {
            List<Address> addresses = addressRepository.findAllByAccount_AccountId(account.get().getAccountId());
            List<AddressInfoDTO> addressInfoDTOList = addresses.stream()
                    .map(this::convertToAddressInfoDTO)
                    .collect(Collectors.toList());
            return ApiResponse.<List<AddressInfoDTO>>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(addressInfoDTOList)
                    .timestamp(new java.util.Date())
                    .build();
        }

        return ApiResponse.<List<AddressInfoDTO>>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();

    }

    @Override
    public ApiResponse<List<AddressInfoDTO>> setDefaultAddress(Long addressId) {
        Optional<Account> account = accountService.findCurrentUserInfo();
        if (account.isPresent()) {
            Optional<Address> address = addressRepository.findByAccount_AccountIdAndAddressId(account.get().getAccountId(), addressId);
            if (address.isPresent()) {
                List<Address> addresses = addressRepository.findAllByAccount_AccountId(account.get().getAccountId());
                for (Address address1 : addresses) {
                    address1.setIsDefault(address1.getAddressId().equals(address.get().getAddressId()));
                    addressRepository.save(address1);
                }
            } else {
                return ApiResponse.<List<AddressInfoDTO>>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.ADDRESS_NOT_FOUND)
                        .timestamp(new java.util.Date())
                        .build();
            }
            return getAllAddress();
        }

        return ApiResponse.<List<AddressInfoDTO>>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<AddressInfoDTO> getAddressInfo(Long addressId) {
        Optional<Account> account = accountService.findCurrentUserInfo();
        if (account.isPresent()) {
            Optional<Address> address = addressRepository.findByAccount_AccountIdAndAddressId(account.get().getAccountId(), addressId);
            if (address.isPresent()) {
                return ApiResponse.<AddressInfoDTO>builder()
                        .status(ErrorCode.SUCCESS)
                        .message(Message.SUCCESS)
                        .data(convertToAddressInfoDTO(address.get()))
                        .timestamp(new java.util.Date())
                        .build();
            } else {
                return ApiResponse.<AddressInfoDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.ADDRESS_NOT_FOUND)
                        .timestamp(new java.util.Date())
                        .build();
            }
        }
        return ApiResponse.<AddressInfoDTO>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    @Override
    public ApiResponse<List<AddressInfoDTO>> updateAddressInfo(UpdateAddressRequest request) {
        Optional<Account> account = accountService.findCurrentUserInfo();
        if (account.isPresent()) {
            Optional<Address> address = addressRepository.findByAccount_AccountIdAndAddressId(account.get().getAccountId(), request.getAddress_id());
            if (address.isPresent()) {
                address.get().setFullName(request.getFullname());
                address.get().setPhone(request.getPhone());
                address.get().setAddressDetail(request.getAddress_detail());
                addressRepository.save(address.get());
                return getAllAddress();
            } else {
                return ApiResponse.<List<AddressInfoDTO>>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message(Message.ADDRESS_NOT_FOUND)
                        .timestamp(new java.util.Date())
                        .build();
            }
        }

        return ApiResponse.<List<AddressInfoDTO>>builder()
                .status(ErrorCode.FORBIDDEN)
                .message(Message.ACCOUNT_NOT_FOUND)
                .timestamp(new java.util.Date())
                .build();
    }

    private AddressInfoDTO convertToAddressInfoDTO(Address address) {
        return new AddressInfoDTO(address.getAddressId(), address.getFullName(), address.getPhone(), address.getAddressDetail(), address.getIsDefault());
    }
}
