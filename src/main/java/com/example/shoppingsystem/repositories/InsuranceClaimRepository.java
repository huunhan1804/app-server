package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.InsuranceClaim;
import com.example.shoppingsystem.entities.InsuranceClaim.ClaimStatus;
import com.example.shoppingsystem.entities.InsuranceClaim.SeverityLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long>,
        JpaSpecificationExecutor<InsuranceClaim> {

    Optional<InsuranceClaim> findByClaimCode(String claimCode);

    Page<InsuranceClaim> findByClaimStatus(ClaimStatus status, Pageable pageable);

    // Query cho customer (account có role = 'customer')
    @Query("SELECT ic FROM InsuranceClaim ic " +
            "JOIN ic.customer c " +
            "JOIN c.role r " +
            "WHERE c.accountId = :customerId AND r.roleCode = 'customer'")
    Page<InsuranceClaim> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    // Query cho agency (account có role = 'agency')
    @Query("SELECT ic FROM InsuranceClaim ic " +
            "JOIN ic.agency a " +
            "JOIN a.role r " +
            "WHERE a.accountId = :agencyId AND r.roleCode = 'agency'")
    Page<InsuranceClaim> findByAgencyId(@Param("agencyId") Long agencyId, Pageable pageable);

    // Query để lấy claims theo agency thông qua product
    @Query("SELECT ic FROM InsuranceClaim ic " +
            "JOIN ic.product p " +
            "JOIN p.account a " +
            "JOIN a.role r " +
            "WHERE a.accountId = :agencyId AND r.roleCode = 'agency'")
    Page<InsuranceClaim> findByProductAgencyId(@Param("agencyId") Long agencyId, Pageable pageable);

    @Query("SELECT COUNT(ic) FROM InsuranceClaim ic WHERE ic.claimStatus = :status")
    Long countByStatus(@Param("status") ClaimStatus status);

    @Query("SELECT COUNT(ic) FROM InsuranceClaim ic WHERE ic.severityLevel = :severity AND ic.claimStatus = :status")
    Long countBySeverityLevelAndClaimStatus(@Param("severity") SeverityLevel severity, @Param("status") ClaimStatus status);

    @Query("SELECT COUNT(ic) FROM InsuranceClaim ic WHERE ic.claimStatus = :status AND ic.processedDate >= :date")
    Long countByClaimStatusAndProcessedDateAfter(@Param("status") ClaimStatus status, @Param("date") LocalDateTime date);

    @Query("SELECT ic FROM InsuranceClaim ic WHERE ic.submittedDate BETWEEN :startDate AND :endDate")
    List<InsuranceClaim> findBySubmittedDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Thêm query để tìm customer và agency accounts
    @Query("SELECT a FROM Account a JOIN a.role r WHERE r.roleCode = :roleCode")
    List<Account> findAccountsByRole(@Param("roleCode") String roleCode);
}