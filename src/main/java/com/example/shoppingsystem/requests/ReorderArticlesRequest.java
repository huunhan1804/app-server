package com.example.shoppingsystem.requests;

import lombok.Data;

import java.util.List;

@Data
public class ReorderArticlesRequest {
    private List<Long> articleIds;
}
