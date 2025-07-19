package com.example.shoppingsystem.services.interfaces;

public interface AdminAuthService {
    boolean authenticateAdmin(String username, String password);
    boolean isAdmin(String username);
}