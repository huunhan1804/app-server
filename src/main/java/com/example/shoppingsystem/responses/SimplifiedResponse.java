package com.example.shoppingsystem.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SimplifiedResponse<T> {
    private List<T> content;
    private int size;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}
