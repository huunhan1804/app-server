package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalStatusRepository extends JpaRepository<ApprovalStatus, Long> {
    ApprovalStatus findApprovalStatusByStatusId(Long statusId);
    ApprovalStatus findApprovalStatusByStatusCode(String statusCode);
    ApprovalStatus findApprovalStatusByStatusName(String statusName);

}
