package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.dtos.InsuranceClaimDTO;
import com.example.shoppingsystem.entities.InsuranceClaim.ClaimStatus;
import com.example.shoppingsystem.requests.CreateInsuranceClaimRequest;
import com.example.shoppingsystem.requests.ProcessInsuranceClaimRequest;
import com.example.shoppingsystem.requests.SendClaimCommunicationRequest;
import com.example.shoppingsystem.responses.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InsuranceClaimService {
    ApiResponse<InsuranceClaimDTO> createClaim(CreateInsuranceClaimRequest request);
    ApiResponse<InsuranceClaimDTO> getClaimDetail(Long claimId);
    ApiResponse<Page<InsuranceClaimDTO>> getAllClaims(Pageable pageable, ClaimStatus status, String keyword);
    ApiResponse<InsuranceClaimDTO> updateClaimStatus(Long claimId, ProcessInsuranceClaimRequest request);
    ApiResponse<Void> sendCommunication(SendClaimCommunicationRequest request);
    ApiResponse<Page<InsuranceClaimDTO>> getClaimsByCustomer(Long customerId, Pageable pageable);
    ApiResponse<Page<InsuranceClaimDTO>> getClaimsByAgency(Long agencyId, Pageable pageable);
}