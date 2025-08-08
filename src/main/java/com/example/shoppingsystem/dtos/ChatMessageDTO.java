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
public class ChatMessageDTO {
    private Long chatMessageId;
    private Long chatSessionId;
    private Long senderId;
    private Long receiverId;
    private String messageContent;
    private LocalDateTime sendAt;
    private Boolean isRead;
}
