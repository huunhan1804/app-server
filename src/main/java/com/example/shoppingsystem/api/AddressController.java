package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.AddressInfoDTO;
import com.example.shoppingsystem.requests.AddAddressRequest;
import com.example.shoppingsystem.requests.UpdateAddressRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
@Tag(name = "Address")
public class AddressController {
    private final AddressService addressService;

    @Operation(summary = "API get info address", description = "This API will get info of address for account.")
    @GetMapping("/info/{addressId}")
    public ResponseEntity<ApiResponse<AddressInfoDTO>> getAddressInfo(@PathVariable long addressId) {
        ApiResponse<AddressInfoDTO> apiResponse = addressService.getAddressInfo(addressId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API add address", description = "This API will add address for account.")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<List<AddressInfoDTO>>> addAddress(@RequestBody AddAddressRequest request) {
        ApiResponse<List<AddressInfoDTO>> apiResponse = addressService.addAddress(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API get all address", description = "This API get all address of current account.")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AddressInfoDTO>>> addAddress() {
        ApiResponse<List<AddressInfoDTO>> apiResponse = addressService.getAllAddress();
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "API get all address", description = "This API get all address of current account.")
    @GetMapping("/set-default/{addressId}")
    public ResponseEntity<ApiResponse<List<AddressInfoDTO>>> addAddress(@PathVariable Long addressId) {
        ApiResponse<List<AddressInfoDTO>> apiResponse = addressService.setDefaultAddress(addressId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }
    @Operation(summary = "API update address", description = "This API update address of current account.")
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<List<AddressInfoDTO>>> updateAddress(@RequestBody UpdateAddressRequest addressId) {
        ApiResponse<List<AddressInfoDTO>> apiResponse = addressService.updateAddressInfo(addressId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }
}
