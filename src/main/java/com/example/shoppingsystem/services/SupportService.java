import com.example.shoppingsystem.entities.SupportTicket;
import com.example.shoppingsystem.entities.InsuranceClaim;
import com.example.shoppingsystem.repositories.SupportTicketRepository;
import com.example.shoppingsystem.repositories.InsuranceClaimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SupportService {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private InsuranceClaimRepository insuranceClaimRepository;

    public Map<String, Object> getAllTickets(Pageable pageable, String status, String priority) {
        Specification<SupportTicket> spec = createTicketSpecification(status, priority);
        Page<SupportTicket> ticketsPage = supportTicketRepository.findAll(spec, pageable);

        List<Map<String, Object>> ticketList = new ArrayList<>();

        for (SupportTicket ticket : ticketsPage.getContent()) {
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("ticketId", ticket.getTicketId());
            ticketData.put("ticketCode", ticket.getTicketCode());
            ticketData.put("subject", ticket.getSubject());
            ticketData.put("description", ticket.getDescription());
            ticketData.put("category", ticket.getCategory());
            ticketData.put("priority", ticket.getPriority());
            ticketData.put("status", ticket.getStatus());
            ticketData.put("createdDate", ticket.getCreatedDate());

            if (ticket.getAccount() != null) {
                ticketData.put("customerName", ticket.getAccount().getFullname());
                ticketData.put("customerEmail", ticket.getAccount().getEmail());
            }

            ticketList.add(ticketData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("tickets", Map.of(
                "content", ticketList,
                "totalElements", ticketsPage.getTotalElements(),
                "totalPages", ticketsPage.getTotalPages(),
                "number", ticketsPage.getNumber(),
                "size", ticketsPage.getSize()
        ));

        return result;
    }

    public Map<String, Object> getInsuranceClaims(Pageable pageable, String status) {
        Specification<InsuranceClaim> spec = createClaimSpecification(status);
        Page<InsuranceClaim> claimsPage = insuranceClaimRepository.findAll(spec, pageable);

        List<Map<String, Object>> claimList = new ArrayList<>();

        for (InsuranceClaim claim : claimsPage.getContent()) {
            Map<String, Object> claimData = new HashMap<>();
            claimData.put("claimId", claim.getClaimId());
            claimData.put("claimType", claim.getClaimType());
            claimData.put("claimDescription", claim.getClaimDescription());
            claimData.put("claimAmount", claim.getClaimAmount());
            claimData.put("claimStatus", claim.getClaimStatus());
            claimData.put("createdDate", claim.getCreatedDate());

            if (claim.getAccount() != null) {
                claimData.put("customerName", claim.getAccount().getFullname());
                claimData.put("customerEmail", claim.getAccount().getEmail());
            }

            if (claim.getProduct() != null) {
                claimData.put("productName", claim.getProduct().getProductName());
            }

            if (claim.getOrderList() != null) {
                claimData.put("orderCode", claim.getOrderList().getOrderCode());
            }

            claimList.add(claimData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("claims", Map.of(
                "content", claimList,
                "totalElements", claimsPage.getTotalElements(),
                "totalPages", claimsPage.getTotalPages(),
                "number", claimsPage.getNumber(),
                "size", claimsPage.getSize()
        ));

        return result;
    }

    public Map<String, Object> getClaimDetail(Long claimId) {
        Optional<InsuranceClaim> claimOpt = insuranceClaimRepository.findById(claimId);

        if (!claimOpt.isPresent()) {
            throw new RuntimeException("Insurance claim not found");
        }

        InsuranceClaim claim = claimOpt.get();
        Map<String, Object> claimDetail = createDetailedClaimMap(claim);

        Map<String, Object> result = new HashMap<>();
        result.put("claim", claimDetail);

        return result;
    }

    public Map<String, Object> approveClaim(Long claimId, Map<String, Object> request) {
        Optional<InsuranceClaim> claimOpt = insuranceClaimRepository.findById(claimId);

        if (!claimOpt.isPresent()) {
            throw new RuntimeException("Insurance claim not found");
        }

        InsuranceClaim claim = claimOpt.get();

        // Extract approval data
        BigDecimal approvedAmount = new BigDecimal(request.get("approvedAmount").toString());
        String payoutMethod = (String) request.get("payoutMethod");
        String adminNotes = (String) request.get("adminNotes");

        claim.setClaimStatus(InsuranceClaim.ClaimStatus.APPROVED);
        claim.setPayoutAmount(approvedAmount);
        claim.setPayoutMethod(payoutMethod);
        claim.setAdminNotes(adminNotes);
        claim.setApprovalDate(LocalDateTime.now());
        claim.setApprovedBy(1L); // Admin ID

        insuranceClaimRepository.save(claim);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Insurance claim approved successfully");
        result.put("claim", createDetailedClaimMap(claim));

        return result;
    }

    public Map<String, Object> rejectClaim(Long claimId, String reason) {
        Optional<InsuranceClaim> claimOpt = insuranceClaimRepository.findById(claimId);

        if (!claimOpt.isPresent()) {
            throw new RuntimeException("Insurance claim not found");
        }

        InsuranceClaim claim = claimOpt.get();
        claim.setClaimStatus(InsuranceClaim.ClaimStatus.REJECTED);
        claim.setRejectionReason(reason);
        claim.setApprovalDate(LocalDateTime.now());
        claim.setApprovedBy(1L); // Admin ID

        insuranceClaimRepository.save(claim);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Insurance claim rejected successfully");
        result.put("claim", createDetailedClaimMap(claim));

        return result;
    }

    private Specification<SupportTicket> createTicketSpecification(String status, String priority) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"),
                        SupportTicket.Status.valueOf(status.toUpperCase())));
            }

            if (priority != null && !priority.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("priority"),
                        SupportTicket.Priority.valueOf(priority.toUpperCase())));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<InsuranceClaim> createClaimSpecification(String status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("claimStatus"),
                        InsuranceClaim.ClaimStatus.valueOf(status.toUpperCase())));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Map<String, Object> createDetailedClaimMap(InsuranceClaim claim) {
        Map<String, Object> claimData = new HashMap<>();
        claimData.put("claimId", claim.getClaimId());
        claimData.put("claimType", claim.getClaimType());
        claimData.put("claimDescription", claim.getClaimDescription());
        claimData.put("claimAmount", claim.getClaimAmount());
        claimData.put("claimStatus", claim.getClaimStatus());
        claimData.put("medicalEvidence", claim.getMedicalEvidence());
        claimData.put("identityProof", claim.getIdentityProof());
        claimData.put("additionalDocuments", claim.getAdditionalDocuments());
        claimData.put("adminNotes", claim.getAdminNotes());
        claimData.put("rejectionReason", claim.getRejectionReason());
        claimData.put("payoutAmount", claim.getPayoutAmount());
        claimData.put("payoutMethod", claim.getPayoutMethod());
        claimData.put("createdDate", claim.getCreatedDate());
        claimData.put("approvalDate", claim.getApprovalDate());

        if (claim.getAccount() != null) {
            claimData.put("customerName", claim.getAccount().getFullname());
            claimData.put("customerEmail", claim.getAccount().getEmail());
        }

        if (claim.getProduct() != null) {
            claimData.put("productName", claim.getProduct().getProductName());
        }

        if (claim.getOrderList() != null) {
            claimData.put("orderCode", claim.getOrderList().getOrderCode());
        }

        return claimData;
    }
}