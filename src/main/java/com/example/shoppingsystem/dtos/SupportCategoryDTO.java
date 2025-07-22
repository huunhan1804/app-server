package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SupportCategoryDTO {
    private Long supportCategoryId;
    private String supportCategoryName;
    private String supportCategoryDescription;
}
