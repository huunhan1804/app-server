package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByCustomer_AccountIdOrAgency_AccountId(Long accountId, Long agencyId);
}
