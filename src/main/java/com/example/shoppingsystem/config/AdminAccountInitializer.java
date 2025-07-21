package com.example.shoppingsystem.config;

import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.ApprovalStatus;
import com.example.shoppingsystem.entities.Role;
import com.example.shoppingsystem.repositories.AccountRepository;
import com.example.shoppingsystem.repositories.ApprovalStatusRepository;
import com.example.shoppingsystem.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminAccountInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final ApprovalStatusRepository approvalStatusRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeApprovalStatuses();
        initializeRoles();
        initializeAdminAccount();
    }

    private void initializeApprovalStatuses() {
        // Tạo approval status approved nếu chưa có
        if (approvalStatusRepository.findApprovalStatusByStatusCode("approved") == null) {
            ApprovalStatus approvedStatus = ApprovalStatus.builder()
                    .statusCode("approved")
                    .statusName("Approved")
                    .build();
            approvalStatusRepository.save(approvedStatus);
            log.info("Created approved status");
        }

        // Tạo approval status pending nếu chưa có
        if (approvalStatusRepository.findApprovalStatusByStatusCode("pending") == null) {
            ApprovalStatus pendingStatus = ApprovalStatus.builder()
                    .statusCode("pending")
                    .statusName("Pending")
                    .build();
            approvalStatusRepository.save(pendingStatus);
            log.info("Created pending status");
        }

        // Tạo approval status rejected nếu chưa có
        if (approvalStatusRepository.findApprovalStatusByStatusCode("rejected") == null) {
            ApprovalStatus rejectedStatus = ApprovalStatus.builder()
                    .statusCode("rejected")
                    .statusName("Rejected")
                    .build();
            approvalStatusRepository.save(rejectedStatus);
            log.info("Created rejected status");
        }
    }

    private void initializeRoles() {
        // Tạo role admin nếu chưa có
        if (roleRepository.findByRoleCode("admin") == null) {
            Role adminRole = Role.builder()
                    .roleCode("admin")
                    .roleName("Administrator")
                    .description("System Administrator")
                    .build();
            roleRepository.save(adminRole);
            log.info("Created admin role");
        }

        // Tạo role customer nếu chưa có
        if (roleRepository.findByRoleCode("customer") == null) {
            Role customerRole = Role.builder()
                    .roleCode("customer")
                    .roleName("Customer")
                    .description("Customer User")
                    .build();
            roleRepository.save(customerRole);
            log.info("Created customer role");
        }

        // Tạo role agency nếu chưa có
        if (roleRepository.findByRoleCode("agency") == null) {
            Role agencyRole = Role.builder()
                    .roleCode("agency")
                    .roleName("Agency")
                    .description("Agency User")
                    .build();
            roleRepository.save(agencyRole);
            log.info("Created agency role");
        }
    }

    private void initializeAdminAccount() {
        // Kiểm tra xem đã có admin chưa
        if (!accountRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByRoleCode("admin");
            ApprovalStatus approvedStatus = approvalStatusRepository.findApprovalStatusByStatusCode("approved");

            if (adminRole != null && approvedStatus != null) {
                Account adminAccount = Account.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("admin@shoppingsystem.com")
                        .fullname("System Administrator")
                        .phone("0123456789")
                        .gender("Male")
                        .birthdate(new Date())
                        .role(adminRole)
                        .approvalStatus(approvedStatus)
                        .accountStatus(Account.AccountStatus.ACTIVE)
                        .isBanned(false)
                        .build();
                accountRepository.save(adminAccount);
                log.info("Created default admin account - Username: admin, Password: admin123");
            } else {
                log.error("Failed to create admin account: Role or ApprovalStatus not found");
            }
        }
    }
}