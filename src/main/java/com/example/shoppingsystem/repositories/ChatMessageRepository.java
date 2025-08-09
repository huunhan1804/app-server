package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.ChatMessage;
import com.example.shoppingsystem.entities.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatSessionOrderBySendAtAsc(ChatSession chatSession);
    ChatMessage findFirstByChatSessionOrderBySendAtDesc(ChatSession chatSession);
}
