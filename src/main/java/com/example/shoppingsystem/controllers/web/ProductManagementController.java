//package com.example.shoppingsystem.controllers.web;
//
//import com.example.shoppingsystem.dtos.ProductManagementDTO;
//import com.example.shoppingsystem.services.interfaces.ProductManagementService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//@Controller
//@RequestMapping("/admin/products")
//@RequiredArgsConstructor
//public class ProductManagementController {
//
//    private final ProductManagementService productManagementService;
//
//    @GetMapping
//    public String productManagement(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "createdDate") String sortBy,
//            @RequestParam(defaultValue = "desc") String sortDir,
//            @RequestParam(required = false) String status,
//            @RequestParam(required = false) String category,
//            @RequestParam(required = false) String agency,
//            @RequestParam(required = false) String keyword,
//            Model model) {
//
//        Sort sort = sortDir.equalsIgnoreCase("desc") ?
//                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
//
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        Page<ProductManagementDTO> products = productManagementService.getAllProducts(
//                pageable, status, category, agency, keyword);
//
//        model.addAttribute("products", products);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", products.getTotalPages());
//        model.addAttribute("totalElements", products.getTotalElements());
//        model.addAttribute("size", size);
//        model.addAttribute("sortBy", sortBy);
//        model.addAttribute("sortDir", sortDir);
//        model.addAttribute("status", status);
//        model.addAttribute("category", category);
//        model.addAttribute("agency", agency);
//        model.addAttribute("keyword", keyword);
//
//        // Load filter options
//        model.addAttribute("categories", productManagementService.getAllCategories());
//        model.addAttribute("agencies", productManagementService.getAllAgencies());
//        model.addAttribute("statuses", productManagementService.getAllProductStatuses());
//
//        return "admin/products/management";
//    }
//
//    @GetMapping("/pending")
//    public String pendingProducts(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            Model model) {
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
//        Page<ProductManagementDTO> pendingProducts = productManagementService.getPendingProducts(pageable);
//
//        model.addAttribute("products", pendingProducts);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", pendingProducts.getTotalPages());
//        model.addAttribute("totalElements", pendingProducts.getTotalElements());
//        model.addAttribute("size", size);
//
//        return "admin/products/pending";
//    }
//
//    @GetMapping("/review/{productId}")
//    public String reviewProduct(@PathVariable Long productId, Model model) {
//        ProductManagementDTO product = productManagementService.getProductForReview(productId);
//        model.addAttribute("product", product);
//        return "admin/products/review";
//    }
//
//    @PostMapping("/approve/{productId}")
//    public String approveProduct(@PathVariable Long productId, RedirectAttributes redirectAttributes) {
//        try {
//            productManagementService.approveProduct(productId);
//            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được phê duyệt thành công!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
//        }
//        return "redirect:/admin/products/pending";
//    }
//
//    @PostMapping("/reject/{productId}")
//    public String rejectProduct(
//            @PathVariable Long productId,
//            @RequestParam String rejectionReason,
//            RedirectAttributes redirectAttributes) {
//        try {
//            productManagementService.rejectProduct(productId, rejectionReason);
//            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã bị từ chối!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
//        }
//        return "redirect:/admin/products/pending";
//    }
//
//    @PostMapping("/request-edit/{productId}")
//    public String requestEdit(
//            @PathVariable Long productId,
//            @RequestParam String editNotes,
//            RedirectAttributes redirectAttributes) {
//        try {
//            productManagementService.requestProductEdit(productId, editNotes);
//            redirectAttributes.addFlashAttribute("success", "Đã gửi yêu cầu chỉnh sửa đến Agency!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
//        }
//        return "redirect:/admin/products/pending";
//    }
//
//    @PostMapping("/remove/{productId}")
//    public String removeProduct(
//            @PathVariable Long productId,
//            @RequestParam String removeReason,
//            RedirectAttributes redirectAttributes) {
//        try {
//            productManagementService.removeProduct(productId, removeReason);
//            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được gỡ bỏ!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
//        }
//        return "redirect:/admin/products";
//    }
//
//    @PostMapping("/warn-agency/{productId}")
//    public String warnAgency(
//            @PathVariable Long productId,
//            @RequestParam String warningMessage,
//            RedirectAttributes redirectAttributes) {
//        try {
//            productManagementService.warnAgency(productId, warningMessage);
//            redirectAttributes.addFlashAttribute("success", "Đã gửi cảnh báo đến Agency!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
//        }
//        return "redirect:/admin/products";
//    }
//}