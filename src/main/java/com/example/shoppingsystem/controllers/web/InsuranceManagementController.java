package com.example.shoppingsystem.controllers.web;

import com.example.shoppingsystem.dtos.InsuranceClaimDTO;
import com.example.shoppingsystem.entities.InsuranceClaim.ClaimStatus;
import com.example.shoppingsystem.requests.ProcessInsuranceClaimRequest;
import com.example.shoppingsystem.requests.SendClaimCommunicationRequest;
import com.example.shoppingsystem.services.interfaces.InsuranceClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/insurance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InsuranceManagementController {

    private final InsuranceClaimService insuranceClaimService;

    @GetMapping
    public String insuranceManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            Model model) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        ClaimStatus claimStatus = status != null ? ClaimStatus.valueOf(status) : null;

        Page<InsuranceClaimDTO> claims = insuranceClaimService
                .getAllClaims(pageable, claimStatus, keyword).getData();

        model.addAttribute("claims", claims);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", claims.getTotalPages());
        model.addAttribute("totalElements", claims.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);

        // Load filter options
        model.addAttribute("statuses", ClaimStatus.values());

        return "admin/insurance/management";
    }

    @GetMapping("/review/{claimId}")
    public String reviewClaim(@PathVariable Long claimId, Model model) {
        InsuranceClaimDTO claim = insuranceClaimService.getClaimDetail(claimId).getData();
        model.addAttribute("claim", claim);
        return "admin/insurance/review";
    }

    @PostMapping("/process/{claimId}")
    public String processClaim(
            @PathVariable Long claimId,
            @ModelAttribute ProcessInsuranceClaimRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            insuranceClaimService.updateClaimStatus(claimId, request);
            redirectAttributes.addFlashAttribute("success", "Xử lý yêu cầu bồi thường thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/insurance";
    }

    @PostMapping("/communication")
    @ResponseBody
    public String sendCommunication(@RequestBody SendClaimCommunicationRequest request) {
        try {
            insuranceClaimService.sendCommunication(request);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
}