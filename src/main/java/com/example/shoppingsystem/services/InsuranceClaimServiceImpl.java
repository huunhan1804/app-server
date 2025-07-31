package com.example.shoppingsystem.services;

import com.example.shoppingsystem.auth.interfaces.EmailService;
import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.dtos.InsuranceClaimCommunicationDTO;
import com.example.shoppingsystem.dtos.InsuranceClaimDTO;
import com.example.shoppingsystem.entities.*;
import com.example.shoppingsystem.entities.InsuranceClaim.ClaimStatus;
import com.example.shoppingsystem.repositories.*;
import com.example.shoppingsystem.requests.CreateInsuranceClaimRequest;
import com.example.shoppingsystem.requests.ProcessInsuranceClaimRequest;
import com.example.shoppingsystem.requests.SendClaimCommunicationRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.InsuranceClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsuranceClaimServiceImpl implements InsuranceClaimService {

    private final InsuranceClaimRepository insuranceClaimRepository;
    private final InsuranceClaimCommunicationRepository communicationRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public ApiResponse<InsuranceClaimDTO> createClaim(CreateInsuranceClaimRequest request) {
        try {
            // Tìm customer từ authentication context
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<Account> customerOpt = accountRepository.findByUsername(username);

            if (!customerOpt.isPresent()) {
                return ApiResponse.<InsuranceClaimDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message("Không tìm thấy thông tin khách hàng")
                        .build();
            }

            Account customer = customerOpt.get();

            // Kiểm tra customer có đúng role không
            if (!"customer".equals(customer.getRole().getRoleCode())) {
                return ApiResponse.<InsuranceClaimDTO>builder()
                        .status(ErrorCode.FORBIDDEN)
                        .message("Chỉ khách hàng mới có thể tạo yêu cầu bảo hiểm")
                        .build();
            }

            // Tìm product
            Optional<Product> productOpt = productRepository.findById(request.getProductId());
            if (!productOpt.isPresent()) {
                return ApiResponse.<InsuranceClaimDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message("Không tìm thấy sản phẩm")
                        .build();
            }

            Product product = productOpt.get();
            Account agency = product.getAgencyInfo().getAccount(); // Agency là owner của product

            // Kiểm tra agency có đúng role không
            if (!"agency".equals(agency.getRole().getRoleCode())) {
                return ApiResponse.<InsuranceClaimDTO>builder()
                        .status(ErrorCode.INTERNAL_SERVER_ERROR)
                        .message("Sản phẩm không thuộc vList agency hợp lệ")
                        .build();
            }

            // Tạo mã claim unique
            String claimCode = generateClaimCode();

            // Tạo insurance claim
            InsuranceClaim claim = InsuranceClaim.builder()
                    .claimCode(claimCode)
                    .customer(customer)
                    .product(product)
                    .agency(agency) // Set agency là owner của product
                    .claimTitle(request.getClaimTitle())
                    .claimDescription(request.getClaimDescription())
                    .severityLevel(InsuranceClaim.SeverityLevel.valueOf(request.getSeverityLevel()))
                    .claimStatus(ClaimStatus.SUBMITTED)
                    .customerIdCardFrontUrl(request.getCustomerIdCardFrontUrl())
                    .customerIdCardBackUrl(request.getCustomerIdCardBackUrl())
                    .medicalBillUrls(request.getMedicalBillUrls() != null ?
                            String.join(",", request.getMedicalBillUrls()) : null)
                    .testResultUrls(request.getTestResultUrls() != null ?
                            String.join(",", request.getTestResultUrls()) : null)
                    .doctorReportUrls(request.getDoctorReportUrls() != null ?
                            String.join(",", request.getDoctorReportUrls()) : null)
                    .otherEvidenceUrls(request.getOtherEvidenceUrls() != null ?
                            String.join(",", request.getOtherEvidenceUrls()) : null)
                    .submittedDate(LocalDateTime.now())
                    .build();

            if (request.getOrderId() != null) {
                Optional<OrderList> orderOpt = orderRepository.findById(request.getOrderId());
                orderOpt.ifPresent(claim::setOrder);
            }

            claim = insuranceClaimRepository.save(claim);

            // Gửi email thông báo cho admin
            sendNotificationToAdmin(claim);

            return ApiResponse.<InsuranceClaimDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message("Tạo yêu cầu bồi thường thành công")
                    .data(convertToDTO(claim))
                    .build();

        } catch (Exception e) {
            return ApiResponse.<InsuranceClaimDTO>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message("Có lỗi xảy ra khi tạo yêu cầu bồi thường: " + e.getMessage())
                    .build();
        }
    }
    
    
    @Override
    public ApiResponse<InsuranceClaimDTO> getClaimDetail(Long claimId) {
        Optional<InsuranceClaim> claimOpt = insuranceClaimRepository.findById(claimId);

        if (!claimOpt.isPresent()) {
            return ApiResponse.<InsuranceClaimDTO>builder()
                    .status(ErrorCode.NOT_FOUND)
                    .message("Không tìm thấy yêu cầu bồi thường")
                    .build();
        }

        return ApiResponse.<InsuranceClaimDTO>builder()
                .status(ErrorCode.SUCCESS)
                .message("Lấy thông tin chi tiết thành công")
                .data(convertToDTO(claimOpt.get()))
                .build();
    }

    @Override
    public ApiResponse<Page<InsuranceClaimDTO>> getAllClaims(Pageable pageable, ClaimStatus status, String keyword) {
        Specification<InsuranceClaim> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("claimStatus"), status));
        }

        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("claimCode")), "%" + keyword.toLowerCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("claimTitle")), "%" + keyword.toLowerCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("customer").get("fullname")), "%" + keyword.toLowerCase() + "%")
                    ));
        }

        Page<InsuranceClaim> claims = insuranceClaimRepository.findAll(spec, pageable);
        List<InsuranceClaimDTO> claimDTOs = claims.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ApiResponse.<Page<InsuranceClaimDTO>>builder()
                .status(ErrorCode.SUCCESS)
                .message("Lấy danh sách yêu cầu bồi thường thành công")
                .data(new PageImpl<>(claimDTOs, pageable, claims.getTotalElements()))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<InsuranceClaimDTO> updateClaimStatus(Long claimId, ProcessInsuranceClaimRequest request) {
        try {
            Optional<InsuranceClaim> claimOpt = insuranceClaimRepository.findById(claimId);

            if (!claimOpt.isPresent()) {
                return ApiResponse.<InsuranceClaimDTO>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message("Không tìm thấy yêu cầu bồi thường")
                        .build();
            }

            String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<Account> adminOpt = accountRepository.findByUsername(adminUsername);

            if (!adminOpt.isPresent()) {
                return ApiResponse.<InsuranceClaimDTO>builder()
                        .status(ErrorCode.UNAUTHORIZED)
                        .message("Không có quyền xử lý yêu cầu")
                        .build();
            }

            InsuranceClaim claim = claimOpt.get();
            Account admin = adminOpt.get();

            // Cập nhật thông tin claim
            claim.setClaimStatus(ClaimStatus.valueOf(request.getClaimStatus()));
            claim.setProcessedBy(admin);
            claim.setProcessedDate(LocalDateTime.now());
            claim.setAdminNotes(request.getAdminNotes());

            if (request.getCompensationAmount() != null) {
                claim.setCompensationAmount(request.getCompensationAmount());
            }

            if (request.getCompensationType() != null) {
                claim.setCompensationType(InsuranceClaim.CompensationType.valueOf(request.getCompensationType()));
            }

            if (request.getRejectionReason() != null) {
                claim.setRejectionReason(request.getRejectionReason());
            }

            claim = insuranceClaimRepository.save(claim);

            // Gửi email thông báo kết quả
            sendProcessResultNotification(claim);

            return ApiResponse.<InsuranceClaimDTO>builder()
                    .status(ErrorCode.SUCCESS)
                    .message("Cập nhật trạng thái yêu cầu thành công")
                    .data(convertToDTO(claim))
                    .build();

        } catch (Exception e) {
            return ApiResponse.<InsuranceClaimDTO>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message("Có lỗi xảy ra khi xử lý yêu cầu: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> sendCommunication(SendClaimCommunicationRequest request) {
        try {
            Optional<InsuranceClaim> claimOpt = insuranceClaimRepository.findById(request.getClaimId());
            if (!claimOpt.isPresent()) {
                return ApiResponse.<Void>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message("Không tìm thấy yêu cầu bồi thường")
                        .build();
            }

            String senderUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<Account> senderOpt = accountRepository.findByUsername(senderUsername);

            Optional<Account> recipientOpt = accountRepository.findById(request.getRecipientId());

            if (!senderOpt.isPresent() || !recipientOpt.isPresent()) {
                return ApiResponse.<Void>builder()
                        .status(ErrorCode.NOT_FOUND)
                        .message("Không tìm thấy thông tin người gửi hoặc người nhận")
                        .build();
            }

            InsuranceClaim claim = claimOpt.get();
            Account sender = senderOpt.get();
            Account recipient = recipientOpt.get();

            // Lưu communication record
            InsuranceClaimCommunication communication = InsuranceClaimCommunication.builder()
                    .insuranceClaim(claim)
                    .sender(sender)
                    .recipient(recipient)
                    .emailSubject(request.getEmailSubject())
                    .emailContent(request.getEmailContent())
                    .communicationType(InsuranceClaimCommunication.CommunicationType.valueOf(request.getCommunicationType()))
                    .sentDate(LocalDateTime.now())
                    .isRead(false)
                    .build();

            communicationRepository.save(communication);

            // Gửi email
            emailService.sendInsuranceClaimCommunication(
                    recipient.getEmail(),
                    recipient.getFullname(),
                    request.getEmailSubject(),
                    request.getEmailContent(),
                    claim.getClaimCode()
            );

            return ApiResponse.<Void>builder()
                    .status(ErrorCode.SUCCESS)
                    .message("Gửi email liên lạc thành công")
                    .build();

        } catch (Exception e) {
            return ApiResponse.<Void>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message("Có lỗi xảy ra khi gửi email: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public ApiResponse<Page<InsuranceClaimDTO>> getClaimsByCustomer(Long customerId, Pageable pageable) {
        Page<InsuranceClaim> claims = insuranceClaimRepository.findByCustomerId(customerId, pageable);
        List<InsuranceClaimDTO> claimDTOs = claims.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ApiResponse.<Page<InsuranceClaimDTO>>builder()
                .status(ErrorCode.SUCCESS)
                .message("Lấy danh sách yêu cầu của khách hàng thành công")
                .data(new PageImpl<>(claimDTOs, pageable, claims.getTotalElements()))
                .build();
    }

    @Override
    public ApiResponse<Page<InsuranceClaimDTO>> getClaimsByAgency(Long agencyId, Pageable pageable) {
        Page<InsuranceClaim> claims = insuranceClaimRepository.findByAgencyId(agencyId, pageable);
        List<InsuranceClaimDTO> claimDTOs = claims.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ApiResponse.<Page<InsuranceClaimDTO>>builder()
                .status(ErrorCode.SUCCESS)
                .message("Lấy danh sách yêu cầu của agency thành công")
                .data(new PageImpl<>(claimDTOs, pageable, claims.getTotalElements()))
                .build();
    }

    // Helper methods
    private String generateClaimCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "IC" + timestamp + String.format("%03d", (int)(Math.random() * 1000));
    }

    private InsuranceClaimDTO convertToDTO(InsuranceClaim claim) {
        List<InsuranceClaimCommunicationDTO> communicationDTOs =
                communicationRepository.findByClaimIdOrderBySentDateDesc(claim.getClaimId())
                        .stream()
                        .map(this::convertCommunicationToDTO)
                        .collect(Collectors.toList());

        return InsuranceClaimDTO.builder()
                .claimId(claim.getClaimId())
                .claimCode(claim.getClaimCode())
                .customerId(claim.getCustomer().getAccountId()) // Thêm customerId
                .customerName(claim.getCustomer().getFullname())
                .customerEmail(claim.getCustomer().getEmail())
                .agencyId(claim.getAgency().getAccountId()) // Thêm agencyId
                .agencyName(claim.getAgency().getFullname())
                .agencyEmail(claim.getAgency().getEmail())
                .productName(claim.getProduct().getProductName())
                .claimTitle(claim.getClaimTitle())
                .claimDescription(claim.getClaimDescription())
                .severityLevel(claim.getSeverityLevel().toString())
                .claimStatus(claim.getClaimStatus().toString())
                .customerIdCardFrontUrl(claim.getCustomerIdCardFrontUrl())
                .customerIdCardBackUrl(claim.getCustomerIdCardBackUrl())
                .medicalBillUrls(claim.getMedicalBillUrls() != null ?
                        Arrays.asList(claim.getMedicalBillUrls().split(",")) : null)
                .testResultUrls(claim.getTestResultUrls() != null ?
                        Arrays.asList(claim.getTestResultUrls().split(",")) : null)
                .doctorReportUrls(claim.getDoctorReportUrls() != null ?
                        Arrays.asList(claim.getDoctorReportUrls().split(",")) : null)
                .otherEvidenceUrls(claim.getOtherEvidenceUrls() != null ?
                        Arrays.asList(claim.getOtherEvidenceUrls().split(",")) : null)
                .compensationAmount(claim.getCompensationAmount())
                .compensationType(claim.getCompensationType() != null ?
                        claim.getCompensationType().toString() : null)
                .rejectionReason(claim.getRejectionReason())
                .adminNotes(claim.getAdminNotes())
                .processedByName(claim.getProcessedBy() != null ?
                        claim.getProcessedBy().getFullname() : null)
                .processedDate(claim.getProcessedDate())
                .submittedDate(claim.getSubmittedDate())
                .communications(communicationDTOs)
                .build();
    }

    private InsuranceClaimCommunicationDTO convertCommunicationToDTO(InsuranceClaimCommunication comm) {
        return InsuranceClaimCommunicationDTO.builder()
                .communicationId(comm.getCommunicationId())
                .claimId(comm.getInsuranceClaim().getClaimId())
                .senderName(comm.getSender().getFullname())
                .senderEmail(comm.getSender().getEmail())
                .recipientName(comm.getRecipient().getFullname())
                .recipientEmail(comm.getRecipient().getEmail())
                .emailSubject(comm.getEmailSubject())
                .emailContent(comm.getEmailContent())
                .communicationType(comm.getCommunicationType().toString())
                .sentDate(comm.getSentDate())
                .isRead(comm.getIsRead())
                .build();
    }

    private void sendNotificationToAdmin(InsuranceClaim claim) {
        try {
            // Tìm admin accounts dựa trên role
            List<Account> adminAccounts = accountRepository.findByRole_RoleCode("admin");

            for (Account admin : adminAccounts) {
                emailService.sendInsuranceClaimNotification(
                        admin.getEmail(),
                        admin.getFullname(),
                        "Yêu cầu bồi thường mới - " + claim.getClaimCode(),
                        String.format("Khách hàng %s đã tạo yêu cầu bồi thường mới cho sản phẩm %s từ agency %s. " +
                                        "Mức độ: %s. Vui lòng kiểm tra và xử lý.",
                                claim.getCustomer().getFullname(),
                                claim.getProduct().getProductName(),
                                claim.getAgency().getFullname(),
                                claim.getSeverityLevel()),
                        claim.getClaimCode()
                );
            }
        } catch (Exception e) {
            System.err.println("Error sending notification to admin: " + e.getMessage());
        }
    }

    private void sendProcessResultNotification(InsuranceClaim claim) {
        try {
            String subject = "";
            String message = "";

            switch (claim.getClaimStatus()) {
                case APPROVED:
                    subject = "Yêu cầu bồi thường được chấp thuận - " + claim.getClaimCode();
                    message = String.format("Yêu cầu bồi thường của bạn đã được chấp thuận. " +
                                    "Mức bồi thường: %s %s. Chi tiết: %s",
                            claim.getCompensationAmount(),
                            claim.getCompensationType(),
                            claim.getAdminNotes());
                    break;
                case REJECTED:
                    subject = "Yêu cầu bồi thường bị từ chối - " + claim.getClaimCode();
                    message = String.format("Yêu cầu bồi thường của bạn đã bị từ chối. " +
                                    "Lý do: %s",
                            claim.getRejectionReason());
                    break;
                case PENDING_DOCUMENTS:
                    subject = "Cần bổ sung tài liệu - " + claim.getClaimCode();
                    message = String.format("Yêu cầu bồi thường của bạn cần bổ sung tài liệu. " +
                                    "Chi tiết: %s",
                            claim.getAdminNotes());
                    break;
            }

            // Gửi cho customer
            emailService.sendInsuranceClaimNotification(
                    claim.getCustomer().getEmail(),
                    claim.getCustomer().getFullname(),
                    subject,
                    message,
                    claim.getClaimCode()
            );

            // Gửi cho agency
            emailService.sendInsuranceClaimNotification(
                    claim.getAgency().getEmail(),
                    claim.getAgency().getFullname(),
                    "Cập nhật yêu cầu bồi thường - " + claim.getClaimCode(),
                    String.format("Yêu cầu bồi thường cho sản phẩm %s đã được cập nhật trạng thái: %s",
                            claim.getProduct().getProductName(),
                            claim.getClaimStatus()),
                    claim.getClaimCode()
            );

        } catch (Exception e) {
            System.err.println("Error sending process result notification: " + e.getMessage());
        }
    }
}