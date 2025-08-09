package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.Message;
import com.example.shoppingsystem.constants.RoleCode;
import com.example.shoppingsystem.dtos.ChatBasicDTO;
import com.example.shoppingsystem.dtos.ChatMessageDTO;
import com.example.shoppingsystem.dtos.ChatSessionDTO;
import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.ChatMessage;
import com.example.shoppingsystem.entities.ChatSession;
import com.example.shoppingsystem.repositories.*;
import com.example.shoppingsystem.requests.CreateChatSessionRequest;
import com.example.shoppingsystem.requests.SendMessageRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.ChatSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ChatSupportServiceImpl implements ChatSupportService {
    ChatSessionRepository chatSessionRepository;
    ChatMessageRepository chatMessageRepository;
    AccountRepository accountRepository;
    MultimediaRepository multimediaRepository;
    AgencyInfoRepository agencyInfoRepository;

    @Autowired
    public ChatSupportServiceImpl(ChatSessionRepository chatSessionRepository, ChatMessageRepository chatMessageRepository, AccountRepository accountRepository, MultimediaRepository multimediaRepository, AgencyInfoRepository agencyInfoRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.accountRepository = accountRepository;
        this.multimediaRepository = multimediaRepository;
        this.agencyInfoRepository = agencyInfoRepository;
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

    @Override
    public ApiResponse<List<ChatSessionDTO>> getChatSessionByUser(Long userId) {
        Optional<Account> account = accountRepository.findById(userId);
        if (account.isPresent()) {
            List<ChatSession> chatSessions = chatSessionRepository.findByCustomer_AccountIdOrAgency_AccountId(userId, userId);
            List<ChatSessionDTO> chatSessionDTOS = chatSessions.stream().map(this::convertChatSessionDTO).toList();
            return ApiResponse.<List<ChatSessionDTO>>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(chatSessionDTOS)
                    .timestamp(new Date())
                    .build();
        }
        return ApiResponse.<List<ChatSessionDTO>>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.AGENCY_NOT_FOUND)
                .timestamp(new Date())
                .build();
    }

    @Override
    public ApiResponse<List<ChatMessageDTO>> getChatMessageByChatSession(Long chatSessionId){
        Optional<ChatSession> chatSession = chatSessionRepository.findById(chatSessionId);
        if (chatSession.isPresent()) {
            List<ChatMessage> messages = chatMessageRepository.findByChatSessionOrderBySendAtAsc(chatSession.get());
            List<ChatMessageDTO> chatMessageDTOS = messages.stream().map(this::convertChatMessageDTO).toList();
            return ApiResponse.<List<ChatMessageDTO>>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(chatMessageDTOS)
                    .timestamp(new Date())
                    .build();
        }
        return ApiResponse.<List<ChatMessageDTO>>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.NOT_FOUND_CHAT_SESSION)
                .timestamp(new Date())
                .build();
    }

    @Override
    public ApiResponse<Void> markMessageAsRead(Long messageId) {
        Optional<ChatMessage> chatMessage = chatMessageRepository.findById(messageId);
        if (chatMessage.isPresent()) {
            ChatMessage message = chatMessage.get();
            message.setRead(true);
            chatMessageRepository.save(message);
            return ApiResponse.<Void>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .timestamp(new Date())
                    .build();
        }
        return ApiResponse.<Void>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.NOT_FOUND_CHAT_MESSAGE)
                .timestamp(new Date())
                .build();
    }

    @Override
    public ApiResponse<List> getAllChatSessionsByUser(Long userId){
        Optional<Account> account = accountRepository.findById(userId);
        if (account.isPresent()) {
            List<ChatSession> chatSessions = chatSessionRepository.findByCustomer_AccountIdOrAgency_AccountId(userId, userId);
            return ApiResponse.<List>builder()
                    .status(ErrorCode.SUCCESS)
                    .message(Message.SUCCESS)
                    .data(chatSessions.stream().map(chatSession -> convertChatBasicDTO(chatSession.getChatSessionId(), userId)).toList())
                    .timestamp(new Date())
                    .build();
        }
        return ApiResponse.<List>builder()
                .status(ErrorCode.NOT_FOUND)
                .message(Message.AGENCY_NOT_FOUND)
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

    private ChatBasicDTO convertChatBasicDTO(Long chatSessionId, Long senderId){
        ChatBasicDTO chatBasicDTO = new ChatBasicDTO();
        chatBasicDTO.setChatSessionId(chatSessionId);

        Account account = accountRepository.findById(senderId).orElse(null);
        chatBasicDTO.setSenderAvatar(Objects.requireNonNull(multimediaRepository.findByAccount(account).orElse(null)).getMultimediaUrl());
        assert account != null;
        if(account.getRole().getRoleCode().equals(RoleCode.ROLE_CUSTOMER)){
            chatBasicDTO.setSenderName(account.getFullname());
        }
        if(account.getRole().getRoleCode().equals(RoleCode.ROLE_AGENCY)){
            chatBasicDTO.setSenderName(Objects.requireNonNull(agencyInfoRepository.findByAccount(account).orElse(null)).getShopName());
        }

        ChatMessage newMessage = chatMessageRepository.findFirstByChatSessionOrderBySendAtDesc(chatSessionRepository.findById(chatSessionId).get());
        chatBasicDTO.setNewMessage(newMessage.getMessageContent());
        chatBasicDTO.setSendTime(newMessage.getSendAt());

        return chatBasicDTO;
    }
}
