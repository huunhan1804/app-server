package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long>, JpaSpecificationExecutor<SupportTicket> {

    Page<SupportTicket> findByStatus(SupportTicket.Status status, Pageable pageable);

    Page<SupportTicket> findByPriority(SupportTicket.Priority priority, Pageable pageable);

    Page<SupportTicket> findByStatusAndPriority(SupportTicket.Status status, SupportTicket.Priority priority, Pageable pageable);

    @Query("SELECT COUNT(st) FROM SupportTicket st WHERE st.status = 'OPEN'")
    Long countOpenTickets();

    @Query("SELECT st FROM SupportTicket st WHERE st.accountId = :accountId ORDER BY st.createdDate DESC")
    List<SupportTicket> findByAccountIdOrderByCreatedDateDesc(@Param("accountId") Long accountId);
}