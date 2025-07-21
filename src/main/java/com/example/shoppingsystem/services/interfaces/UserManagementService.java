// UserManagementService.java
package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.AgencyApplicationDetailDTO;
import com.example.shoppingsystem.dtos.UserManagementDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserManagementService {
    // Customer Management
    Page<UserManagementDTO> getAllCustomers(Pageable pageable, String status, String membershipLevel, String keyword);
    UserManagementDTO getCustomerDetail(Long accountId);
    void suspendCustomer(Long accountId, String reason);
    void activateCustomer(Long accountId);
    void resetCustomerPassword(Long accountId);

    // Agency Management
    Page<UserManagementDTO> getAllAgencies(Pageable pageable, String approvalStatus, String keyword);
    UserManagementDTO getAgencyDetail(Long accountId);
    void suspendAgency(Long accountId, String reason);
    void activateAgency(Long accountId);

    // Agency Application Review
    Page<AgencyApplicationDetailDTO> getPendingApplications(Pageable pageable);
    AgencyApplicationDetailDTO getApplicationDetail(Long applicationId);
    void approveApplication(Long applicationId, String adminUsername);
    void declineApplication(Long applicationId, String reason, String adminUsername);

    // Filters and options
    List<String> getAllAccountStatuses();
    List<String> getAllMembershipLevels();
    List<String> getAllApprovalStatuses();
}