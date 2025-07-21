package com.example.shoppingsystem.requests;

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
    private long account_id;
    private String shop_name;
    private String shop_address;
    private String shop_phone;
    private String shop_email;
    private String tax_code;

    private String full_name_applicant;
    private LocalDate birth_date_applicant;
    private String gender_applicant;
    private String id_card_number_applicant;
    private LocalDate date_of_issue_card;
    private String place_of_issue_card;

}
