package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionDTO {
    private Long chatSessionId;
    private Long customerId;
    private Long agencyId;
    private LocalDateTime startedAt;
    private LocalDateTime lastUpdated;
}
