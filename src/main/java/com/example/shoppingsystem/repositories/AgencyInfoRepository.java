package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.AgencyInfo;
import com.example.shoppingsystem.entities.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgencyInfoRepository extends JpaRepository<AgencyInfo, Long> {
    Optional<AgencyInfo> findByApprovalStatus_StatusCode(String status_code);
    Optional<AgencyInfo> findByAccount_AccountId(Long accountAccountId);
    Optional<AgencyInfo> findByApplicationId(Long applicationId);
    Optional<AgencyInfo> findByShopName(String shopName);
    Optional<AgencyInfo> findByIdCardNumber(String idCardNumber);
    Page<AgencyInfo> findByBusinessNameContaining(String businessName, Pageable pageable);

    Page<AgencyInfo> findByApprovalStatus(String status, Pageable pageable);

    Page<AgencyInfo> findByBusinessNameContainingAndApprovalStatus(String businessName, String status, Pageable pageable);

    @Query("SELECT COUNT(ai) FROM AgencyInfo ai WHERE ai.approvalStatus = :status")
    Long countByApprovalStatus(@Param("status") String status);

    @Query("SELECT ai FROM AgencyInfo ai WHERE ai.approvalStatus = 'WAITING' ORDER BY ai.createdDate ASC")
    List<AgencyInfo> getPendingApprovals();

    Optional<AgencyInfo> findByAccountId(Long accountId);
}
