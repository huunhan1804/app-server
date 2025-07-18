package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUsername(String username);
    Optional<Account> findByPhone(String phone);
    List<Account> findByRoleRoleId(Long roleId);
    Page<Account> findAll(Specification<Account> specificationAccount, Pageable pageable);
    Account findByAccountId(Long accountId);
    Optional<Account> findByApprovalStatus_StatusCode(String approvalStatus);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.role.roleId = :roleId")
    Long countByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.role.roleId = 2 AND MONTH(a.createdDate) = MONTH(CURRENT_DATE) AND YEAR(a.createdDate) = YEAR(CURRENT_DATE)")
    Long countNewUsersThisMonth();

    @Query("SELECT a FROM Account a WHERE a.role.roleId = :roleId ORDER BY a.createdDate DESC")
    Page<Account> findByRoleId(@Param("roleId") Long roleId, Pageable pageable);

    @Query("SELECT a FROM Account a WHERE a.role.roleId = 2 AND (a.fullname LIKE %:search% OR a.email LIKE %:search% OR a.username LIKE %:search%) ORDER BY a.createdDate DESC")
    Page<Account> findCustomersWithSearch(@Param("search") String search, Pageable pageable);

    @Query("SELECT a FROM Account a ORDER BY a.createdDate DESC")
    List<Account> getRecentUsers(int limit);

    @Query("SELECT NEW map(ol.orderId as orderCode, ol.orderDate as orderDate, ol.totalPrice as finalAmount, ol.orderStatus as orderStatus) " +
            "FROM OrderList ol WHERE ol.account.accountId = :accountId ORDER BY ol.orderDate DESC")
    List<Map<String, Object>> getOrderHistory(@Param("accountId") Long accountId);

    @Query("SELECT NEW map(COUNT(ol) as totalOrders, SUM(ol.totalPrice) as totalSpent, " +
            "SUM(CASE WHEN ol.orderStatus = 'DELIVERED' THEN 1 ELSE 0 END) as completedOrders, " +
            "SUM(CASE WHEN ol.orderStatus = 'CANCELLED' THEN 1 ELSE 0 END) as cancelledOrders) " +
            "FROM OrderList ol WHERE ol.account.accountId = :accountId")
    Map<String, Object> getOrderStats(@Param("accountId") Long accountId);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.role.roleId = 2 AND MONTH(a.createdDate) = MONTH(:date) AND YEAR(a.createdDate) = YEAR(:date)")
    Long getCustomerCountByMonth(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.role.roleId = 3 AND MONTH(a.createdDate) = MONTH(:date) AND YEAR(a.createdDate) = YEAR(:date)")
    Long getAgencyCountByMonth(@Param("date") LocalDate date);
}
