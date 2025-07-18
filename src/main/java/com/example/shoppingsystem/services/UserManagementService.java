package com.example.shoppingsystem.services;

import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.AgencyInfo;
import com.example.shoppingsystem.repositories.AccountRepository;
import com.example.shoppingsystem.repositories.AgencyInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserManagementService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AgencyInfoRepository agencyInfoRepository;

    public Page<Account> getCustomers(Pageable pageable, String search) {
        if (search != null && !search.isEmpty()) {
            return accountRepository.findCustomersWithSearch(search, pageable);
        }
        return accountRepository.findByRoleId(2L, pageable);
    }

    public Map<String, Object> getAgencies(Pageable pageable, String search, String status) {
        Page<AgencyInfo> agencies;

        if (search != null && !search.isEmpty() && status != null && !status.isEmpty()) {
            agencies = agencyInfoRepository.findByBusinessNameContainingAndApprovalStatus(search, status, pageable);
        } else if (search != null && !search.isEmpty()) {
            agencies = agencyInfoRepository.findByBusinessNameContaining(search, pageable);
        } else if (status != null && !status.isEmpty()) {
            agencies = agencyInfoRepository.findByApprovalStatus(status, pageable);
        } else {
            agencies = agencyInfoRepository.findAll(pageable);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("agencies", agencies);
        result.put("totalElements", agencies.getTotalElements());
        result.put("totalPages", agencies.getTotalPages());
        result.put("currentPage", agencies.getNumber());

        return result;
    }

    public Map<String, Object> getPendingAgencies() {
        Page<AgencyInfo> pendingAgencies = agencyInfoRepository.findByApprovalStatus("WAITING", Pageable.unpaged());

        Map<String, Object> result = new HashMap<>();
        result.put("pendingAgencies", pendingAgencies.getContent());
        result.put("count", pendingAgencies.getTotalElements());

        return result;
    }

    public Map<String, Object> approveAgency(Long agencyId) {
        Optional<AgencyInfo> agencyOpt = agencyInfoRepository.findById(agencyId);

        if (!agencyOpt.isPresent()) {
            throw new RuntimeException("Agency not found");
        }

        AgencyInfo agency = agencyOpt.get();
        agency.setApprovalStatus(AgencyInfo.ApprovalStatus.APPROVED);
        agency.setApprovalDate(LocalDateTime.now());
        agency.setApprovedBy(1L); // Admin ID

        agencyInfoRepository.save(agency);

        // Update account status
        Account account = agency.getAccount();
        account.setAccountStatus(Account.AccountStatus.ACTIVE);
        accountRepository.save(account);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Agency approved successfully");
        result.put("agency", agency);

        return result;
    }

    public Map<String, Object> rejectAgency(Long agencyId, String reason) {
        Optional<AgencyInfo> agencyOpt = agencyInfoRepository.findById(agencyId);

        if (!agencyOpt.isPresent()) {
            throw new RuntimeException("Agency not found");
        }

        AgencyInfo agency = agencyOpt.get();
        agency.setApprovalStatus(AgencyInfo.ApprovalStatus.REJECTED);
        agency.setRejectionReason(reason);

        agencyInfoRepository.save(agency);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Agency rejected successfully");
        result.put("agency", agency);

        return result;
    }

    public Map<String, Object> getUserDetail(Long userId) {
        Optional<Account> accountOpt = accountRepository.findById(userId);

        if (!accountOpt.isPresent()) {
            throw new RuntimeException("User not found");
        }

        Account account = accountOpt.get();
        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put("account", account);

        // If agency, get agency info
        if (account.getRoleId() == 3L) {
            Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccountId(userId);
            userDetail.put("agencyInfo", agencyInfo.orElse(null));
        }

        // Get order history
        userDetail.put("orderHistory", accountRepository.getOrderHistory(userId));
        userDetail.put("orderStats", accountRepository.getOrderStats(userId));

        return userDetail;
    }

    public Map<String, Object> updateUserStatus(Long userId, String status) {
        Optional<Account> accountOpt = accountRepository.findById(userId);

        if (!accountOpt.isPresent()) {
            throw new RuntimeException("User not found");
        }

        Account account = accountOpt.get();
        account.setAccountStatus(Account.AccountStatus.valueOf(status));
        accountRepository.save(account);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "User status updated successfully");
        result.put("account", account);

        return result;
    }
}
