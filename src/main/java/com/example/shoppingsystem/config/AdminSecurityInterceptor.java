package com.example.shoppingsystem.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

//@Component
public class AdminSecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // Bỏ qua login page và static resources
        if (requestURI.equals("/admin/login") ||
                requestURI.startsWith("/admin/css/") ||
                requestURI.startsWith("/admin/js/") ||
                requestURI.startsWith("/admin/images/") ||
                requestURI.startsWith("/admin/static/")) {
            return true;
        }

        // Kiểm tra authentication cho admin routes
        if (requestURI.startsWith("/admin/")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                response.sendRedirect("/admin/login");
                return false;
            }

            // Kiểm tra có role admin không
            boolean hasAdminRole = auth.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("admin"));

            if (!hasAdminRole) {
                response.sendRedirect("/admin/login?error=access_denied");
                return false;
            }
        }

        return true;
    }
}