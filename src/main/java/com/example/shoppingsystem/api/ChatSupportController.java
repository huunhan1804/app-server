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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PostMapping("/message")
    public ResponseEntity<ApiResponse<ChatMessageDTO>> sendMessage(@RequestBody SendMessageRequest request) {
        ApiResponse<ChatMessageDTO> apiResponse = chatSupportService.sendMessage(request);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "Get chat session by user", description = "This is API for getting chat session by user")
    @PostMapping("/session/user/{userId}")
    public ResponseEntity<ApiResponse<List<ChatSessionDTO>>> getChatSessionByUserId(@PathVariable Long userId) {
        ApiResponse<List<ChatSessionDTO>> apiResponse = chatSupportService.getChatSessionByUser(userId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "Get all messages of chat session", description = "This is API for getting all messages of chat session")
    @PostMapping("/message/session/{sessionId}")
    public ResponseEntity<ApiResponse<List<ChatMessageDTO>>> getAllChatMessages(@PathVariable Long sessionId) {
        ApiResponse<List<ChatMessageDTO>> apiResponse = chatSupportService.getChatMessageByChatSession(sessionId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "Mark message as read", description = "This is API for marking message as read")
    @PutMapping("/message/read/{messageId}")
    public ResponseEntity<ApiResponse<Void>> markMessageAsRead(@PathVariable Long messageId) {
        ApiResponse<Void> apiResponse = chatSupportService.markMessageAsRead(messageId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @Operation(summary = "Get chat list by user", description = "This is API for getting chat list by user")
    @PostMapping("/all-by-user/{userId}")
    public ResponseEntity<ApiResponse<List>> getAllChatSessionsByUser(@PathVariable Long userId) {
        ApiResponse<List> apiResponse = chatSupportService.getAllChatSessionsByUser(userId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }
}
