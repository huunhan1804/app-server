package com.example.shoppingsystem.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewUserDTO {
    private String fullName;
    private String email;
    private String role;
    private LocalDateTime joinDate;
}
