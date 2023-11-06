package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.AddressInfoDTO;
import com.example.shoppingsystem.requests.AddAddressRequest;
import com.example.shoppingsystem.requests.UpdateAddressRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface AddressService {
    ApiResponse<List<AddressInfoDTO>> addAddress(AddAddressRequest request);
    ApiResponse<List<AddressInfoDTO>> getAllAddress();
    ApiResponse<List<AddressInfoDTO>> setDefaultAddress(Long addressId);
    ApiResponse<AddressInfoDTO> getAddressInfo(Long addressId);
    ApiResponse<List<AddressInfoDTO>> updateAddressInfo(UpdateAddressRequest request);
}
