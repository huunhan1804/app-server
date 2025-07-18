package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.InsuranceClaim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long>, JpaSpecificationExecutor<InsuranceClaim> {

    Page<InsuranceClaim> findByClaimStatus(InsuranceClaim.ClaimStatus status, Pageable pageable);

    @Query("SELECT COUNT(ic) FROM InsuranceClaim ic WHERE ic.claimStatus = 'SUBMITTED'")
    Long countPendingClaims();

    @Query("SELECT ic FROM InsuranceClaim ic WHERE ic.accountId = :accountId ORDER BY ic.createdDate DESC")
    List<InsuranceClaim> findByAccountIdOrderByCreatedDateDesc(@Param("accountId") Long accountId);

    @Query("SELECT SUM(ic.payoutAmount) FROM InsuranceClaim ic WHERE ic.claimStatus = 'PAID'")
    Long getTotalPayoutAmount();
}
