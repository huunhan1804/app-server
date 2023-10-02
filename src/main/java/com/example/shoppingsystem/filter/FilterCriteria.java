package com.example.shoppingsystem.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FilterCriteria {
    private String key;
    private FilterOperation operation;
    private Object value;
}
