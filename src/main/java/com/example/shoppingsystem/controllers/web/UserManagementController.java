//// UserManagementController.java
//package com.example.shoppingsystem.controllers.web;
//
//import com.example.shoppingsystem.dtos.AgencyApplicationDetailDTO;
//import com.example.shoppingsystem.dtos.UserManagementDTO;
//import com.example.shoppingsystem.services.interfaces.UserManagementService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//@Controller
//@RequestMapping("/admin/users")
//@RequiredArgsConstructor
//public class UserManagementController {
//
//    private final UserManagementService userManagementService;
//
//    // Customer Management
//    @GetMapping("/customers")
//    public String customerManagement(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "createdDate") String sortBy,
//            @RequestParam(defaultValue = "desc") String sortDir,
//            @RequestParam(required = false) String status,
//            @RequestParam(required = false) String membershipLevel,
//            @RequestParam(required = false) String keyword,
//            Model model) {
//
//        Sort sort = sortDir.equalsIgnoreCase("desc") ?
//                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
//
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        Page<UserManagementDTO> customers = userManagementService.getAllCustomers(
//                pageable, status, membershipLevel, keyword);
//
//        model.addAttribute("customers", customers);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", customers.getTotalPages());
//        model.addAttribute("totalElements", customers.getTotalElements());
//        model.addAttribute("size", size);
//        model.addAttribute("sortBy", sortBy);
//        model.addAttribute("sortDir", sortDir);
//        model.addAttribute("status", status);
//        model.addAttribute("membershipLevel", membershipLevel);
//        model.addAttribute("keyword", keyword);
//
//        // Load filter options
//        model.addAttribute("accountStatuses", userManagementService.getAllAccountStatuses());
//        model.addAttribute("membershipLevels", userManagementService.getAllMembershipLevels());
//
//        return "admin/users/customers";
//    }
//
//    // Agency Management
//    @GetMapping("/agencies")
//    public String agencyManagement(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "createdDate") String sortBy,
//            @RequestParam(defaultValue = "desc") String sortDir,
//            @RequestParam(required = false) String approvalStatus,
//            @RequestParam(required = false) String keyword,
//            Model model) {
//
//        Sort sort = sortDir.equalsIgnoreCase("desc") ?
//                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
//
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        Page<UserManagementDTO> agencies = userManagementService.getAllAgencies(
//                pageable, approvalStatus, keyword);
//
//        model.addAttribute("agencies", agencies);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", agencies.getTotalPages());
//        model.addAttribute("totalElements", agencies.getTotalElements());
//        model.addAttribute("size", size);
//        model.addAttribute("sortBy", sortBy);
//        model.addAttribute("sortDir", sortDir);
//        model.addAttribute("approvalStatus", approvalStatus);
//        model.addAttribute("keyword", keyword);
//
//        // Load filter options
//        model.addAttribute("approvalStatuses", userManagementService.getAllApprovalStatuses());
//
//        return "admin/users/agencies";
//    }
//
//    // Agency Applications
//    @GetMapping("/agencies/applications")
//    public String agencyApplications(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            Model model) {
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedDate").descending());
//        Page<AgencyApplicationDetailDTO> applications = userManagementService.getPendingApplications(pageable);
//
//        model.addAttribute("applications", applications);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", applications.getTotalPages());
//        model.addAttribute("totalElements", applications.getTotalElements());
//        model.addAttribute("size", size);
//
//        return "admin/users/agency-applications";
//    }
//
//    @GetMapping("/agencies/applications/{applicationId}")
//    public String reviewApplication(@PathVariable Long applicationId, Model model) {
//        AgencyApplicationDetailDTO application = userManagementService.getApplicationDetail(applicationId);
//        model.addAttribute("application", application);
//        return "admin/users/application-review";
//    }
//
//    @PostMapping("/agencies/applications/{applicationId}/approve")
//    public String approveApplication(@PathVariable Long applicationId, RedirectAttributes redirectAttributes) {
//        try {
//            String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
//            userManagementService.approveApplication(applicationId, adminUsername);
//            redirectAttributes.addFlashAttribute("success", "Đơn đăng ký đã được phê duyệt thành công!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
//        }
//        return "redirect:/admin/users/agencies/applications";
//    }
//
//    @PostMapping("/agencies/applications/{applicationId}/decline")
//    public String declineApplication(
//            @PathVariable Long applicationId,
//            @RequestParam String reason,
//            RedirectAttributes redirectAttributes) {
//        try {
//            String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
//            userManagementService.declineApplication(applicationId, reason, adminUsername);
//            redirectAttributes.addFlashAttribute("success", "Đơn đăng ký đã bị từ chối!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
//        }
//        return "redirect:/admin/users/agencies/applications";
//    }
//
//    // Customer Actions
//    @PostMapping("/customers/{accountId}/suspend")
//    public String suspendCustomer(
//            @PathVariable Long accountId,
//            @RequestParam String reason,
//            RedirectAttributes redirectAttributes) {
//        try {
//            userManagementService.suspendCustomer(accountId, reason);
//            redirectAttributes.addFlashAttribute("success", "Tài khoản đã bị tạm khóa!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
//        }
//        return "redirect:/admin/users/customers";
//    }
//
//    @PostMapping("/customers/{accountId}/activate")
//    public String activateCustomer(@PathVariable Long accountId, RedirectAttributes redirectAttributes) {
//        try {
//            userManagementService.activateCustomer(accountId);
//            redirectAttributes.addFlashAttribute("success", "Tài khoản đã được kích hoạt!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
//        }
//        return "redirect:/admin/users/customers";
//    }
//
//    @PostMapping("/customers/{accountId}/reset-password")
//    public String resetCustomerPassword(@PathVariable Long accountId, RedirectAttributes redirectAttributes) {
//        try {
//            userManagementService.resetCustomerPassword(accountId);
//            redirectAttributes.addFlashAttribute("success", "Mật khẩu đã được reset và gửi qua email!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
//        }
//        return "redirect:/admin/users/customers";
//    }
//
//    // Agency Actions
//    @PostMapping("/agencies/{accountId}/suspend")
//    public String suspendAgency(
//            @PathVariable Long accountId,
//            @RequestParam String reason,
//            RedirectAttributes redirectAttributes) {
//        try {
//            userManagementService.suspendAgency(accountId, reason);
//            redirectAttributes.addFlashAttribute("success", "Agency đã bị tạm khóa!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
//        }
//        return "redirect:/admin/users/agencies";
//    }
//
//    @PostMapping("/agencies/{accountId}/activate")
//    public String activateAgency(@PathVariable Long accountId, RedirectAttributes redirectAttributes) {
//        try {
//            userManagementService.activateAgency(accountId);
//            redirectAttributes.addFlashAttribute("success", "Agency đã được kích hoạt!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
//        }
//        return "redirect:/admin/users/agencies";
//    }
//}