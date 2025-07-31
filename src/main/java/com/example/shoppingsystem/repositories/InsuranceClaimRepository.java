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
}