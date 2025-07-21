package com.example.shoppingsystem.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgencyResponse {
    private Long agencyId;
    private String agencyName;
    private String agencyEmail;
}