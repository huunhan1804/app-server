package com.example.shoppingsystem.dtos;

import com.example.shoppingsystem.entities.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AgencyInfoDTO {
    private long agency_id;
    private long account_id;
    private String agency_name;
    private String agency_address;
    private String agency_phone;
    private String agency_email;
    private String agency_tax_code;
}
