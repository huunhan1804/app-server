// UserManagementServiceImpl.java
package com.example.shoppingsystem.services;

import com.example.shoppingsystem.constants.StatusCode;
import com.example.shoppingsystem.dtos.AgencyApplicationDetailDTO;
import com.example.shoppingsystem.dtos.UserManagementDTO;
import com.example.shoppingsystem.entities.*;
import com.example.shoppingsystem.repositories.*;
import com.example.shoppingsystem.services.interfaces.NotificationService;
import com.example.shoppingsystem.services.interfaces.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final AccountRepository accountRepository;
    private final AgencyInfoRepository agencyInfoRepository;
    private final OrderRepository orderRepository;
    private final MembershipRepository membershipRepository;
    private final ApprovalStatusRepository approvalStatusRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    @Autowired
    private NotificationService notificationService;

    @Override
    public Page<UserManagementDTO> getAllCustomers(Pageable pageable, String status, String membershipLevel, String keyword) {
        Specification<Account> spec = Specification.where(null);

        // Filter by customer role
        spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("role").get("roleCode"), "customer"));

        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("accountStatus"), Account.AccountStatus.valueOf(status)));
        }

        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + keyword.toLowerCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("fullname")), "%" + keyword.toLowerCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + keyword.toLowerCase() + "%")
                    ));
        }

        Page<Account> accounts = accountRepository.findAll(spec, pageable);

        List<UserManagementDTO> userDTOs = accounts.getContent().stream()
                .map(this::convertToCustomerDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(userDTOs, pageable, accounts.getTotalElements());
    }

    @Override
    public Page<UserManagementDTO> getAllAgencies(Pageable pageable, String approvalStatus, String keyword) {
        Specification<Account> spec = Specification.where(null);

        // Filter by agency role
        spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("role").get("roleCode"), "agency"));

        if (approvalStatus != null && !approvalStatus.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("approvalStatus").get("statusCode"), approvalStatus));
        }

        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + keyword.toLowerCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("fullname")), "%" + keyword.toLowerCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + keyword.toLowerCase() + "%")
                    ));
        }

        Page<Account> accounts = accountRepository.findAll(spec, pageable);

        List<UserManagementDTO> userDTOs = accounts.getContent().stream()
                .map(this::convertToAgencyDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(userDTOs, pageable, accounts.getTotalElements());
    }

    @Override
    public Page<AgencyApplicationDetailDTO> getPendingApplications(Pageable pageable) {
        Page<AgencyInfo> applications = agencyInfoRepository.findByApprovalStatus_StatusCode(
                StatusCode.STATUS_PENDING, pageable);

        List<AgencyApplicationDetailDTO> applicationDTOs = applications.getContent().stream()
                .map(this::convertToApplicationDetailDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(applicationDTOs, pageable, applications.getTotalElements());
    }

    @Override
    public AgencyApplicationDetailDTO getApplicationDetail(Long applicationId) {
        AgencyInfo application = agencyInfoRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Đơn đăng ký không tồn tại"));

        return convertToApplicationDetailDTO(application);
    }

    @Override
    @Transactional
    public void approveApplication(Long applicationId, String adminUsername) {
        AgencyInfo application = agencyInfoRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Đơn đăng ký không tồn tại"));

        // Update application status
        ApprovalStatus approvedStatus = approvalStatusRepository.findApprovalStatusByStatusCode(StatusCode.STATUS_APPROVED);
        application.setApprovalStatus(approvedStatus);
        application.setReviewedDate(LocalDateTime.now());
        application.setReviewedBy(adminUsername);
        agencyInfoRepository.save(application);

        // Update account status and role
        Account account = application.getAccount();
        account.setApprovalStatus(approvedStatus);
        account.setRole(roleRepository.findByRoleCode("agency"));
        account.setAccountStatus(Account.AccountStatus.ACTIVE);
        accountRepository.save(account);

        // Send notification to agency
        sendNotificationToAgency(account, "Đơn đăng ký được phê duyệt",
                "Chúc mừng! Đơn đăng ký bán hàng của bạn đã được phê duyệt. Bạn có thể bắt đầu bán hàng ngay bây giờ.");
    }

    @Override
    @Transactional
    public void declineApplication(Long applicationId, String reason, String adminUsername) {
        AgencyInfo application = agencyInfoRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Đơn đăng ký không tồn tại"));

        // Update application status
        ApprovalStatus rejectedStatus = approvalStatusRepository.findApprovalStatusByStatusCode(StatusCode.STATUS_REJECTED);
        application.setApprovalStatus(rejectedStatus);
        application.setRejectionReason(reason);
        application.setReviewedDate(LocalDateTime.now());
        application.setReviewedBy(adminUsername);
        agencyInfoRepository.save(application);

        // Update account status
        Account account = application.getAccount();
        account.setApprovalStatus(rejectedStatus);
        accountRepository.save(account);

        // Send notification to agency
        sendNotificationToAgency(account, "Đơn đăng ký bị từ chối",
                "Đơn đăng ký bán hàng của bạn đã bị từ chối. Lý do: " + reason +
                        ". Vui lòng chỉnh sửa và nộp lại hồ sơ.");
    }

    @Override
    @Transactional
    public void suspendCustomer(Long accountId, String reason) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        account.setIsBanned(true);
        account.setAccountStatus(Account.AccountStatus.SUSPENDED);
        accountRepository.save(account);

        // Send notification
        sendNotificationToUser(account, "Tài khoản bị tạm khóa",
                "Tài khoản của bạn đã bị tạm khóa. Lý do: " + reason);
    }

    @Override
    @Transactional
    public void activateCustomer(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        account.setIsBanned(false);
        account.setAccountStatus(Account.AccountStatus.ACTIVE);
        accountRepository.save(account);

        // Send notification
        sendNotificationToUser(account, "Tài khoản được kích hoạt",
                "Tài khoản của bạn đã được kích hoạt lại. Bạn có thể tiếp tục sử dụng dịch vụ.");
    }

    @Override
    @Transactional
    public void resetCustomerPassword(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        // Generate temporary password
        String tempPassword = generateTemporaryPassword();
        account.setPassword(passwordEncoder.encode(tempPassword));
        accountRepository.save(account);

        // Send notification with new password
        sendNotificationToUser(account, "Mật khẩu đã được reset",
                "Mật khẩu mới của bạn là: " + tempPassword + ". Vui lòng đổi mật khẩu sau khi đăng nhập.");
    }

    @Override
    public UserManagementDTO getCustomerDetail(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
        return convertToCustomerDTO(account);
    }

    @Override
    public UserManagementDTO getAgencyDetail(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
        return convertToAgencyDTO(account);
    }

    @Override
    @Transactional
    public void suspendAgency(Long accountId, String reason) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        account.setIsBanned(true);
        account.setAccountStatus(Account.AccountStatus.SUSPENDED);
        accountRepository.save(account);

        // Send notification
        sendNotificationToAgency(account, "Agency bị tạm khóa",
                "Agency của bạn đã bị tạm khóa. Lý do: " + reason);
    }

    @Override
    @Transactional
    public void activateAgency(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        account.setIsBanned(false);
        account.setAccountStatus(Account.AccountStatus.ACTIVE);
        accountRepository.save(account);

        // Send notification
        sendNotificationToAgency(account, "Agency được kích hoạt",
                "Agency của bạn đã được kích hoạt lại. Bạn có thể tiếp tục bán hàng.");
    }

    // Helper methods
    private UserManagementDTO convertToCustomerDTO(Account account) {
        // Get customer statistics
        List<OrderList> orders = orderRepository.findAllByAccount_AccountId(account.getAccountId());
        BigDecimal totalOrderValue = orders.stream()
                .map(OrderList::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Optional<Membership> membership = membershipRepository.findByAccount_AccountId(account.getAccountId());

        return UserManagementDTO.builder()
                .accountId(account.getAccountId())
                .username(account.getUsername())
                .fullname(account.getFullname())
                .email(account.getEmail())
                .phone(account.getPhone())
                .role(account.getRole().getRoleName())
                .accountStatus(account.getAccountStatus().toString())
                .isBanned(account.isBanned())
                .createdDate(account.getCreatedDate())
                .lastLogin(account.getLastLogin())
                .totalOrderValue(totalOrderValue)
                .totalOrders(orders.size())
                .membershipLevel(membership.map(m -> m.getMembershipLevel().toString()).orElse("BRONZE"))
                .build();
    }

    // UserManagementServiceImpl.java - Sửa method convertToAgencyDTO
    private UserManagementDTO convertToAgencyDTO(Account account) {
        Optional<AgencyInfo> agencyInfo = agencyInfoRepository.findByAccount_AccountId(account.getAccountId());

        // Calculate agency statistics from orders
        BigDecimal totalRevenue = BigDecimal.ZERO;
        try {
            List<OrderList> orders = orderRepository.findAllByAccount_AccountId(account.getAccountId()); // Sửa từ findAllByAgency_AccountId
            totalRevenue = orders.stream()
                    .filter(order -> "DELIVERED".equals(order.getOrderStatus().toString()))
                    .map(OrderList::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            // Log error và continue với default value
            System.err.println("Error calculating revenue for account " + account.getAccountId() + ": " + e.getMessage());
        }

        return UserManagementDTO.builder()
                .accountId(account.getAccountId())
                .username(account.getUsername())
                .fullname(account.getFullname())
                .email(account.getEmail())
                .phone(account.getPhone())
                .role(account.getRole().getRoleName())
                .accountStatus(account.getAccountStatus().toString())
                .isBanned(account.isBanned())
                .createdDate(account.getCreatedDate())
                .lastLogin(account.getLastLogin())
                .businessName(agencyInfo.map(AgencyInfo::getShopName).orElse("Chưa đăng ký"))
                .approvalStatus(account.getApprovalStatus() != null ?
                        account.getApprovalStatus().getStatusName() : "Chưa nộp hồ sơ")
                .totalRevenue(totalRevenue)
                .storeRating(agencyInfo.map(info -> 0.0).orElse(0.0)) // Default rating
                .totalReviews(agencyInfo.map(info -> 0).orElse(0)) // Default reviews
                .submittedDate(agencyInfo.map(AgencyInfo::getSubmittedDate)
                        .map(date -> date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                        .orElse(null))
                .build();
    }

    private AgencyApplicationDetailDTO convertToApplicationDetailDTO(AgencyInfo application) {
        Account account = application.getAccount();

        return AgencyApplicationDetailDTO.builder()
                .applicationId(application.getApplicationId())
                .accountId(account.getAccountId())
                .username(account.getUsername())
                .fullname(account.getFullname())
                .email(account.getEmail())
                .phone(account.getPhone())
                .shopName(application.getShopName())
                .shopAddressDetail(application.getShopAddressDetail())
                .shopEmail(application.getShopEmail())
                .shopPhone(application.getShopPhone())
                .taxNumber(application.getTaxNumber())
                .fullNameApplicant(application.getFullNameApplicant())
                .birthdateApplicant(application.getBirthdateApplicant())
                .genderApplicant(application.getGenderApplicant())
                .idCardNumber(application.getIdCardNumber())
                .idCardFrontImageUrl(application.getIdCardFrontImageUrl())
                .idCardBackImageUrl(application.getIdCardBackImageUrl())
                .businessLicenseUrls(application.getBusinessLicenseUrls())
                .professionalCertUrls(application.getProfessionalCertUrls())
                .statusCode(application.getApprovalStatus().getStatusCode())
                .statusName(application.getApprovalStatus().getStatusName())
                .rejectionReason(application.getRejectionReason())
                .submittedDate(application.getSubmittedDate() != null ?
                        application.getSubmittedDate().toInstant()
                                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .reviewedDate(application.getReviewedDate())
                .reviewedBy(application.getReviewedBy())
                .build();
    }

    private String generateTemporaryPassword() {
        // Generate a random 8-character password
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    private void sendNotificationToAgency(Account agency, String title, String message) {
        notificationService.sendNotificationToAgency(agency, title, message);
    }

    private void sendNotificationToUser(Account user, String title, String message) {
        notificationService.sendNotificationToUser(user, title, message);
    }

    @Override
    public List<String> getAllAccountStatuses() {
        return Arrays.asList("ACTIVE", "INACTIVE", "PENDING", "SUSPENDED");
    }

    @Override
    public List<String> getAllMembershipLevels() {
        return Arrays.asList("BRONZE", "SILVER", "GOLD", "DIAMOND");
    }

    @Override
    public List<String> getAllApprovalStatuses() {
        return Arrays.asList("pending", "approved", "rejected");
    }
}