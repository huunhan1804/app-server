package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUsername(String username);
    Optional<Account> findByPhone(String phone);
    List<Account> findByRoleRoleId(Long roleId);
    Page<Account> findAll(Specification<Account> specificationAccount, Pageable pageable);
    Account findByAccountId(Long accountId);
  //  Optional<Account> findByAccountIdAndIsBannedFalse(Long accountId);
    Optional<Account> findByApprovalStatus_StatusCode(String approvalStatus);
    List<Account> findByCreatedDateAfter(LocalDateTime date);

    boolean existsByUsername(String admin);

    Long countByCreatedDateBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);

    List<Account> findByRole_RoleCode(String admin);
}
