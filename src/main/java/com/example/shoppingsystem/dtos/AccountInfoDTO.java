package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountInfoDTO {
    private Long id;
    private String username;
    private String avatar_url;
    private String role_code;
    private CartDTO cart_info;
    private AccountProfileDTO accountProfileDTO;
}
