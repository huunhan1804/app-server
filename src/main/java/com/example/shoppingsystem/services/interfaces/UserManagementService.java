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

    // ==================== CUSTOMER MANAGEMENT ====================

    /**
     * Get all customers with pagination and filtering
     * @param pageable Pagination information
     * @param status Account status filter (ACTIVE, INACTIVE, PENDING, SUSPENDED)
     * @param membershipLevel Membership level filter (BRONZE, SILVER, GOLD, DIAMOND)
     * @param keyword Search keyword for username, fullname, email
     * @return Page of UserManagementDTO for customers
     */
    Page<UserManagementDTO> getAllCustomers(Pageable pageable, String status, String membershipLevel, String keyword);

    /**
     * Alternative method name for consistency with API controller
     */
    Page<UserManagementDTO> getCustomers(Pageable pageable, String status, String membershipLevel, String keyword);

    /**
     * Get detailed information of a specific customer
     * @param accountId Customer account ID
     * @return UserManagementDTO with customer details
     */
    UserManagementDTO getCustomerDetail(Long accountId);

    /**
     * Suspend a customer account
     * @param accountId Customer account ID
     * @param reason Suspension reason
     */
    void suspendCustomer(Long accountId, String reason);

    /**
     * Activate a suspended customer account
     * @param accountId Customer account ID
     */
    void activateCustomer(Long accountId);

    /**
     * Reset customer password and send new password via notification
     * @param accountId Customer account ID
     */
    void resetCustomerPassword(Long accountId);

    // ==================== AGENCY MANAGEMENT ====================

    /**
     * Get all agencies with pagination and filtering
     * @param pageable Pagination information
     * @param approvalStatus Approval status filter (pending, approved, rejected)
     * @param keyword Search keyword for username, fullname, email, business name
     * @return Page of UserManagementDTO for agencies
     */
    Page<UserManagementDTO> getAllAgencies(Pageable pageable, String approvalStatus, String keyword);

    /**
     * Alternative method name for consistency with API controller
     */
    Page<UserManagementDTO> getAgencies(Pageable pageable, String approvalStatus, String keyword);

    /**
     * Get detailed information of a specific agency
     * @param accountId Agency account ID
     * @return UserManagementDTO with agency details
     */
    UserManagementDTO getAgencyDetail(Long accountId);

    /**
     * Suspend an agency account
     * @param accountId Agency account ID
     * @param reason Suspension reason
     */
    void suspendAgency(Long accountId, String reason);

    /**
     * Activate a suspended agency account
     * @param accountId Agency account ID
     */
    void activateAgency(Long accountId);

    // ==================== AGENCY APPLICATION REVIEW ====================

    /**
     * Get all pending agency applications with pagination
     * @param pageable Pagination information
     * @return Page of AgencyApplicationDetailDTO for pending applications
     */
    Page<AgencyApplicationDetailDTO> getPendingApplications(Pageable pageable);

    /**
     * Get detailed information of a specific agency application
     * @param applicationId Application ID
     * @return AgencyApplicationDetailDTO with application details
     */
    AgencyApplicationDetailDTO getApplicationDetail(Long applicationId);

    /**
     * Approve an agency application
     * @param applicationId Application ID
     * @param adminUsername Username of admin who approved
     */
    void approveApplication(Long applicationId, String adminUsername);

    /**
     * Decline an agency application
     * @param applicationId Application ID
     * @param reason Decline reason
     * @param adminUsername Username of admin who declined
     */
    void declineApplication(Long applicationId, String reason, String adminUsername);

    // ==================== FILTER OPTIONS AND UTILITIES ====================

    /**
     * Get all available account statuses for filtering
     * @return List of account status strings
     */
    List<String> getAllAccountStatuses();

    /**
     * Get all available membership levels for filtering
     * @return List of membership level strings
     */
    List<String> getAllMembershipLevels();

    /**
     * Get all available approval statuses for filtering
     * @return List of approval status strings
     */
    List<String> getAllApprovalStatuses();
}