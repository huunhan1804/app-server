package com.example.shoppingsystem.requests;

import com.example.shoppingsystem.dtos.AgencyRegisterDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgencyRegisterRequest {
    private AgencyRegisterDTO registrationData;
    private String idCardFrontUrl;
    private String idCardBackUrl;
    private String businessLicenseUrl;
    private String professionalCertificateUrl;
    private String diplomaCertificateUrl;

}
