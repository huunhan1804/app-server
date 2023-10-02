package com.example.shoppingsystem.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateAccountRequest {
    private String fullname;
    private String gender;
    private Date birthday;
}
