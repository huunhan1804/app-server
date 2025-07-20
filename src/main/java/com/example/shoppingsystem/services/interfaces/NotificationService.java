package com.example.shoppingsystem.services.interfaces;

import com.example.shoppingsystem.entities.Account;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {
    void sendNotificationToUser(Account user, String title, String message);
    void sendNotificationToAgency(Account agency, String title, String message);
    void sendProductNotification(Account account, String productName, String title, String message);
    void sendSystemNotification(Account account, String title, String message);
}