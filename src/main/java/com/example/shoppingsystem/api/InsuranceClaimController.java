package com.example.shoppingsystem.api;

import com.example.shoppingsystem.dtos.InsuranceClaimDTO;
import com.example.shoppingsystem.entities.InsuranceClaim.ClaimStatus;
import com.example.shoppingsystem.requests.CreateInsuranceClaimRequest;
import com.example.shoppingsystem.requests.ProcessInsuranceClaimRequest;
import com.example.shoppingsystem.requests.SendClaimCommunicationRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import com.example.shoppingsystem.services.interfaces.InsuranceClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insurance-claims")
@RequiredArgsConstructor
public class InsuranceClaimController {

    private final InsuranceClaimService insuranceClaimService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<InsuranceClaimDTO>> createClaim(
            @Valid @RequestBody CreateInsuranceClaimRequest request) {

        ApiResponse<InsuranceClaimDTO> response = insuranceClaimService.createClaim(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{claimId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('AGENCY')")
    public ResponseEntity<ApiResponse<InsuranceClaimDTO>> getClaimDetail(@PathVariable Long claimId) {
        ApiResponse<InsuranceClaimDTO> response = insuranceClaimService.getClaimDetail(claimId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<InsuranceClaimDTO>>> getAllClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        ClaimStatus claimStatus = status != null ? ClaimStatus.valueOf(status) : null;

        ApiResponse<Page<InsuranceClaimDTO>> response =
                insuranceClaimService.getAllClaims(pageable, claimStatus, keyword);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{claimId}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InsuranceClaimDTO>> processClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody ProcessInsuranceClaimRequest request) {

        ApiResponse<InsuranceClaimDTO> response =
                insuranceClaimService.updateClaimStatus(claimId, request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/communication")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendCommunication(
            @Valid @RequestBody SendClaimCommunicationRequest request) {

        ApiResponse<Void> response = insuranceClaimService.sendCommunication(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #customerId == authentication.principal.accountId)")
    public ResponseEntity<ApiResponse<Page<InsuranceClaimDTO>>> getClaimsByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedDate").descending());
        ApiResponse<Page<InsuranceClaimDTO>> response =
                insuranceClaimService.getClaimsByCustomer(customerId, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/agency/{agencyId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('AGENCY') and #agencyId == authentication.principal.accountId)")
    public ResponseEntity<ApiResponse<Page<InsuranceClaimDTO>>> getClaimsByAgency(
            @PathVariable Long agencyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedDate").descending());
        ApiResponse<Page<InsuranceClaimDTO>> response =
                insuranceClaimService.getClaimsByAgency(agencyId, pageable);

        return ResponseEntity.ok(response);
    }
}