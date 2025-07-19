package com.example.shoppingsystem.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgencyRegisterRequest {
    private String shopName;
    private String shopAddress;
    private String shopEmail;
    private String shopPhone;
    private String taxCode;
    private String idCardNumber;

    private String frontIdCardImageUrl;
    private String backIdCardImageUrl;
    private String professionalCertUrl;
    private String businessLicenseUrl;

}
