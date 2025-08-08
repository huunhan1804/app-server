package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.Message;
import com.example.shoppingsystem.dtos.ChatMessageDTO;
import com.example.shoppingsystem.dtos.ChatSessionDTO;
import com.example.shoppingsystem.entities.ChatMessage;
import com.example.shoppingsystem.entities.ChatSession;
import com.example.shoppingsystem.repositories.AccountRepository;
import com.example.shoppingsystem.repositories.ChatMessageRepository;
import com.example.shoppingsystem.repositories.ChatSessionRepository;
import com.example.shoppingsystem.requests.CreateChatSessionRequest;
import com.example.shoppingsystem.requests.SendMessageRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.ChatSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

@Component
public class ChatSupportServiceImpl implements ChatSupportService {
    ChatSessionRepository chatSessionRepository;
    ChatMessageRepository chatMessageRepository;
    AccountRepository accountRepository;

    @Autowired
    public ChatSupportServiceImpl(ChatSessionRepository chatSessionRepository, ChatMessageRepository chatMessageRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public ApiResponse<ChatSessionDTO> createChatSession(CreateChatSessionRequest request){
        ChatSession chatSession = new ChatSession();
        chatSession.setCustomer(accountRepository.findById(request.getCustomerId()).get());
        chatSession.setAgency(accountRepository.findById(request.getAgencyId()).get());
        chatSession.setStartedAt(LocalDateTime.now());
        chatSession.setLastUpdated(LocalDateTime.now());

        ChatSession session = chatSessionRepository.save(chatSession);
        return ApiResponse.<ChatSessionDTO>builder()
                .status(ErrorCode.SUCCESS)
                .message("Create chat session successfully!")
                .data(convertChatSessionDTO(session))
                .timestamp(new Date())
                .build();
    }

    @Override
    public ApiResponse<ChatMessageDTO> sendMessage(SendMessageRequest request){
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatSession(chatSessionRepository.findById(request.getChatSessionId()).get());
        chatMessage.setSender(accountRepository.findById(request.getSenderId()).get());
        chatMessage.setReceiver(accountRepository.findById(request.getReceiverId()).get());
        chatMessage.setMessageContent(request.getMessageContent());
        chatMessage.setSendAt(LocalDateTime.now());
        chatMessage.setRead(false);

        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);

        ChatSession session = chatSessionRepository.findById(request.getChatSessionId()).orElse(null);
        if(session != null){
            session.setLastUpdated(LocalDateTime.now());
            chatSessionRepository.save(session);
        }
        return ApiResponse.<ChatMessageDTO>builder()
                .status(ErrorCode.SUCCESS)
                .message("Create chat session successfully!")
                .data(convertChatMessageDTO(savedChatMessage))
                .timestamp(new Date())
                .build();
    }

    private ChatSessionDTO convertChatSessionDTO(ChatSession chatSession){
        ChatSessionDTO chatSessionDTO = new ChatSessionDTO();
        chatSessionDTO.setChatSessionId(chatSession.getChatSessionId());
        chatSessionDTO.setCustomerId(chatSessionDTO.getCustomerId());
        chatSessionDTO.setAgencyId(chatSessionDTO.getAgencyId());
        chatSessionDTO.setStartedAt(chatSessionDTO.getStartedAt());
        chatSessionDTO.setLastUpdated(chatSessionDTO.getLastUpdated());
        return chatSessionDTO;
    }

    private ChatMessageDTO convertChatMessageDTO(ChatMessage chatMessage){
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setChatMessageId(chatMessage.getChatMessageId());
        chatMessageDTO.setChatSessionId(chatMessage.getChatSession().getChatSessionId());
        chatMessageDTO.setSenderId(chatMessage.getSender().getAccountId());
        chatMessageDTO.setReceiverId(chatMessage.getReceiver().getAccountId());
        chatMessageDTO.setMessageContent(chatMessage.getMessageContent());
        chatMessageDTO.setSendAt(chatMessage.getSendAt());
        chatMessageDTO.setIsRead(false);
        return chatMessageDTO;
    }
}
