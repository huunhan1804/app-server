package com.example.shoppingsystem.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddFeedbackRequest {
    private Long productId;
    private int rating;
    private String comment;
}
