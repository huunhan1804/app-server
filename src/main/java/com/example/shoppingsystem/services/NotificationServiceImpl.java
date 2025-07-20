package com.example.shoppingsystem.services.implementations;

import com.example.shoppingsystem.auth.interfaces.EmailService;
import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.services.interfaces.NotificationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LogManager.getLogger(NotificationServiceImpl.class);

    private final EmailService emailService;

    @Autowired
    public NotificationServiceImpl(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendNotificationToUser(Account user, String title, String message) {
        try {
            logger.info("Sending notification to user: " + user.getEmail() + " - " + title);
            emailService.sendNotificationEmail(user.getEmail(), user.getFullname(), title, message);
            logger.info("Notification sent successfully to user: " + user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send notification to user: " + user.getEmail() + ", error: " + e.getMessage());
        }
    }

    @Override
    public void sendNotificationToAgency(Account agency, String title, String message) {
        try {
            logger.info("Sending notification to agency: " + agency.getEmail() + " - " + title);
            emailService.sendNotificationEmail(agency.getEmail(), agency.getFullname(), title, message);
            logger.info("Notification sent successfully to agency: " + agency.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send notification to agency: " + agency.getEmail() + ", error: " + e.getMessage());
        }
    }

    @Override
    public void sendProductNotification(Account account, String productName, String title, String message) {
        try {
            logger.info("Sending product notification to: " + account.getEmail() + " - " + title);
            emailService.sendProductNotificationEmail(account.getEmail(), account.getFullname(), productName, title, message);
            logger.info("Product notification sent successfully to: " + account.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send product notification to: " + account.getEmail() + ", error: " + e.getMessage());
        }
    }

    @Override
    public void sendSystemNotification(Account account, String title, String message) {
        try {
            logger.info("Sending system notification to: " + account.getEmail() + " - " + title);
            emailService.sendNotificationEmail(account.getEmail(), account.getFullname(), title, message);
            logger.info("System notification sent successfully to: " + account.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send system notification to: " + account.getEmail() + ", error: " + e.getMessage());
        }
    }
}