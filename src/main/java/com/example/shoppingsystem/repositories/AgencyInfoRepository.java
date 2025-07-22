package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.AgencyInfo;
import com.example.shoppingsystem.entities.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgencyInfoRepository extends JpaRepository<AgencyInfo, Long> {
    Optional<AgencyInfo> findByApprovalStatus_StatusCode(String status_code);
    Optional<AgencyInfo> findByAccount_AccountId(Long accountAccountId);
    Optional<AgencyInfo> findByApplicationId(Long applicationId);
    Optional<AgencyInfo> findByShopName(String shopName);
    Optional<AgencyInfo> findByIdCardNumber(String idCardNumber);
    Page<AgencyInfo> findByApprovalStatus_StatusCode(String statusCode, Pageable pageable);

    // Tìm đơn đăng ký của một tài khoản cụ thể
    Optional<AgencyInfo> findByAccount(Account account);

    // Tìm đơn đăng ký mới nhất của một tài khoản, sắp xếp theo ngày gửi giảm dần
    Optional<AgencyInfo> findTopByAccountOrderBySubmittedDateDesc(Account account);
}
