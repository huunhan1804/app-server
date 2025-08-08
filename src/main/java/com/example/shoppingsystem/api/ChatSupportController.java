package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.ChatMessageDTO;
import com.example.shoppingsystem.dtos.ChatSessionDTO;
import com.example.shoppingsystem.requests.CreateChatSessionRequest;
import com.example.shoppingsystem.requests.SendMessageRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.ChatSupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat Support")
public class ChatSupportController {
    private final ChatSupportService chatSupportService;

    @Operation(summary = "API for creating chat support session", description = "This is API for creating chat support session")
    @PostMapping("/session")
    public ResponseEntity<ApiResponse<ChatSessionDTO>> createChatSession(@RequestBody CreateChatSessionRequest request) {
        ApiResponse<ChatSessionDTO> apiResponse = chatSupportService.createChatSession(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "Send message", description = "This is API for sending message")
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ChatMessageDTO>> sendMessage(@RequestBody SendMessageRequest request) {
        ApiResponse<ChatMessageDTO> apiResponse = chatSupportService.sendMessage(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }
}
