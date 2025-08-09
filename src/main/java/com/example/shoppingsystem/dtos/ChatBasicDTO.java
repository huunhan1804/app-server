package com.example.shoppingsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatBasicDTO {
    private long chatSessionId;
    private String senderName;
    private String senderAvatar;
    private String newMessage;
    private LocalDateTime sendTime;
}
