package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AddressInfoDTO {
    private long address_id;
    private String fullname;
    private String phone;
    private String address_detail;
    private boolean is_default;
}
