package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountProfileDTO {
    private String fullname;
    private String email;
    private String phone;
    private String gender;
    private Date birthday;
}
