package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.ChatMessageDTO;
import com.example.shoppingsystem.dtos.ChatSessionDTO;
import com.example.shoppingsystem.requests.CreateChatSessionRequest;
import com.example.shoppingsystem.requests.SendMessageRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import org.springframework.stereotype.Service;

@Service
public interface ChatSupportService {
    ApiResponse<ChatSessionDTO> createChatSession(CreateChatSessionRequest request);
    ApiResponse<ChatMessageDTO> sendMessage(SendMessageRequest request);
}
