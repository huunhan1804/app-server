package com.example.shoppingsystem.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AgencyRegisterDTO {
//    private String shop_name;
//    private String shop_address;
//    private String shop_email;
//    private String shop_phone;
//    private String tax_code;
//
//    private String full_name_applicant;
//    private Date birth_date_applicant;
//    private String gender_applicant;
//    private String id_card_number_applicant;
//    private Date date_of_issue_card;
//    private String place_of_issue_card;
    @JsonProperty("shopName")
    private String shop_name;

    @JsonProperty("address")
    private String shop_address;

    @JsonProperty("email")
    private String shop_email;

    @JsonProperty("phoneNumber")
    private String shop_phone;

    @JsonProperty("taxNumber")
    private String tax_code;

    @JsonProperty("fullName")
    private String full_name_applicant;

    @JsonProperty("gender")
    private String gender_applicant;

    @JsonProperty("idCardNumber")
    private String id_card_number_applicant;

    @JsonProperty("placeOfIssue")
    private String place_of_issue_card;

    @JsonProperty("dateOfBirth")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private Date birth_date_applicant;

    @JsonProperty("dateOfIssue")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private Date date_of_issue_card;
}
