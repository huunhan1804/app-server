package com.example.shoppingsystem.dtos;

import com.example.shoppingsystem.entities.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

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

    private String full_name_applicant;
    private String id_card_number_applicant;
    private String status;
    private String rejectionReason;

    private Date birth_date_applicant;
    private String gender_applicant;
    private Date date_of_issue_card;
    private String place_of_issue_card;
    private String id_card_front_image_url;
    private String id_card_back_image_url;
    private String business_license_urls;
    private String professional_cert_urls;
    private String diploma_cert_urls;
}
