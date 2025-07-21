package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.InsuranceClaimCommunication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceClaimCommunicationRepository extends JpaRepository<InsuranceClaimCommunication, Long> {

    @Query("SELECT icc FROM InsuranceClaimCommunication icc WHERE icc.insuranceClaim.claimId = :claimId ORDER BY icc.sentDate DESC")
    List<InsuranceClaimCommunication> findByClaimIdOrderBySentDateDesc(@Param("claimId") Long claimId);

    @Query("SELECT COUNT(icc) FROM InsuranceClaimCommunication icc WHERE icc.recipient.accountId = :recipientId AND icc.isRead = false")
    Long countUnreadByRecipientId(@Param("recipientId") Long recipientId);
}