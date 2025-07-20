package com.example.shoppingsystem.auth;

import com.example.shoppingsystem.auth.interfaces.EmailService;
import com.example.shoppingsystem.constants.LogMessage;
import com.example.shoppingsystem.enums.OtpPurpose;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LogManager.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void sendPasswordEmail(String email, String password) {
        try {
            logger.info(LogMessage.LOG_PROCESSING_SEND_EMAIL);
            String content = generatePasswordRecoveryContent(password, email);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Khôi phục mật khẩu - CTU SHOP");
            helper.setText(content, true);
            mailSender.send(message);
            logger.info(String.format(LogMessage.LOG_SEND_EMAIL_SUCCESS, email));
            logger.info(LogMessage.LOG_SEND_EMAIL_DONE);
        } catch (MessagingException e) {
            logger.error(String.format(LogMessage.LOG_SEND_EMAIL_FAILED, email, e.getMessage()));
        }
    }

    @Async
    @Override
    public void sendOTP(String email, String otpCode, OtpPurpose otpPurpose) {
        try {
            logger.info(LogMessage.LOG_PROCESSING_SEND_EMAIL);
            String content = generateOtpContent(otpCode, email);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Mã xác thực OTP - CTU SHOP");
            helper.setText(content, true);
            mailSender.send(message);
            logger.info(String.format(LogMessage.LOG_SEND_EMAIL_SUCCESS, email));
            logger.info(LogMessage.LOG_SEND_EMAIL_DONE);
        } catch (MessagingException e) {
            logger.error(String.format(LogMessage.LOG_SEND_EMAIL_FAILED, email, e.getMessage()));
        }
    }

    @Async
    @Override
    public void sendNotificationEmail(String email, String fullName, String title, String message) {
        try {
            logger.info("Processing notification email to: " + email);
            String content = generateNotificationContent(fullName, title, message);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject(title + " - CTU SHOP");
            helper.setText(content, true);
            mailSender.send(mimeMessage);
            logger.info("Notification email sent successfully to: " + email);
        } catch (MessagingException e) {
            logger.error("Failed to send notification email to: " + email + ", error: " + e.getMessage());
        }
    }

    @Async
    @Override
    public void sendProductNotificationEmail(String email, String fullName, String productName, String title, String message) {
        try {
            logger.info("Processing product notification email to: " + email);
            String content = generateProductNotificationContent(fullName, productName, title, message);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject(title + " - CTU SHOP");
            helper.setText(content, true);
            mailSender.send(mimeMessage);
            logger.info("Product notification email sent successfully to: " + email);
        } catch (MessagingException e) {
            logger.error("Failed to send product notification email to: " + email + ", error: " + e.getMessage());
        }
    }

    private String generatePasswordRecoveryContent(String password, String email) {
        return "<div style=\"font-family: Arial, sans-serif; min-width: 1000px; overflow: auto; line-height: 2;\">"
                + "<div style=\"margin: 50px auto; width: 70%; padding: 20px 0;\">"
                + "<div style=\"border-bottom: 1px solid #eee;\">"
                + "<a href=\"\" style=\"font-size: 1.4em; color: #00466a; text-decoration: none; font-weight: 600;\">CTU SHOP</a>"
                + "</div>"
                + "<p style=\"font-size: 1.1em;\">Xin chào, <b>" + email + "</b></p>"
                + "<p>Cảm ơn bạn đã chọn CTU SHOP. Vui lòng sử dụng mật khẩu sau để đặt lại mật khẩu của bạn.</p>"
                + "<h2 style=\"background: #00466a; margin: 0 auto; width: max-content; padding: 0 10px; color: #fff; border-radius: 4px;\">"
                + password
                + "</h2>"
                + "<p style=\"font-size: 0.9em;\">Trân trọng,<br />CTU SHOP</p>"
                + "<hr style=\"border: none; border-top: 1px solid #eee;\" />"
                + "<div style=\"float: right; padding: 8px 0; color: #aaa; font-size: 0.8em; line-height: 1; font-weight: 300;\">"
                + "<p>CTU SHOP</p>"
                + "<p>Thành phố Cần Thơ</p>"
                + "</div>"
                + "</div>"
                + "</div>";
    }

    private String generateOtpContent(String otp, String email) {
        return "<div style=\"font-family: Arial, sans-serif; min-width: 1000px; overflow: auto; line-height: 2;\">"
                + "<div style=\"margin: 50px auto; width: 70%; padding: 20px 0;\">"
                + "<div style=\"border-bottom: 1px solid #eee;\">"
                + "<a href=\"\" style=\"font-size: 1.4em; color: #00466a; text-decoration: none; font-weight: 600;\">CTU SHOP</a>"
                + "</div>"
                + "<p style=\"font-size: 1.1em;\">Xin chào, <b>" + email + "</b></p>"
                + "<p>Cảm ơn bạn đã chọn CTU SHOP. Vui lòng sử dụng mã OTP sau để xác thực tài khoản và tiếp tục.</p>"
                + "<h2 style=\"background: #00466a; margin: 0 auto; width: max-content; padding: 0 10px; color: #fff; border-radius: 4px;\">"
                + otp
                + "</h2>"
                + "<p style=\"font-size: 0.9em;\">Trân trọng,<br />CTU SHOP</p>"
                + "<hr style=\"border: none; border-top: 1px solid #eee;\" />"
                + "<div style=\"float: right; padding: 8px 0; color: #aaa; font-size: 0.8em; line-height: 1; font-weight: 300;\">"
                + "<p>CTU SHOP</p>"
                + "<p>Thành phố Cần Thơ</p>"
                + "</div>"
                + "</div>"
                + "</div>";
    }

    private String generateNotificationContent(String fullName, String title, String message) {
        return "<div style=\"font-family: Arial, sans-serif; min-width: 1000px; overflow: auto; line-height: 2;\">"
                + "<div style=\"margin: 50px auto; width: 70%; padding: 20px 0;\">"
                + "<div style=\"border-bottom: 1px solid #eee;\">"
                + "<a href=\"\" style=\"font-size: 1.4em; color: #00466a; text-decoration: none; font-weight: 600;\">CTU SHOP</a>"
                + "</div>"
                + "<p style=\"font-size: 1.1em;\">Xin chào, <b>" + fullName + "</b></p>"
                + "<h3 style=\"color: #00466a;\">" + title + "</h3>"
                + "<div style=\"background: #f8f9fa; padding: 15px; border-left: 4px solid #00466a; margin: 20px 0;\">"
                + "<p style=\"margin: 0; font-size: 1em;\">" + message + "</p>"
                + "</div>"
                + "<p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>"
                + "<p style=\"font-size: 0.9em;\">Trân trọng,<br />Đội ngũ CTU SHOP</p>"
                + "<hr style=\"border: none; border-top: 1px solid #eee;\" />"
                + "<div style=\"float: right; padding: 8px 0; color: #aaa; font-size: 0.8em; line-height: 1; font-weight: 300;\">"
                + "<p>CTU SHOP</p>"
                + "<p>Thành phố Cần Thơ</p>"
                + "<p>Email: support@fourstore.com</p>"
                + "</div>"
                + "</div>"
                + "</div>";
    }

    private String generateProductNotificationContent(String fullName, String productName, String title, String message) {
        return "<div style=\"font-family: Arial, sans-serif; min-width: 1000px; overflow: auto; line-height: 2;\">"
                + "<div style=\"margin: 50px auto; width: 70%; padding: 20px 0;\">"
                + "<div style=\"border-bottom: 1px solid #eee;\">"
                + "<a href=\"\" style=\"font-size: 1.4em; color: #00466a; text-decoration: none; font-weight: 600;\">CTU SHOP</a>"
                + "</div>"
                + "<p style=\"font-size: 1.1em;\">Xin chào, <b>" + fullName + "</b></p>"
                + "<h3 style=\"color: #00466a;\">" + title + "</h3>"
                + "<div style=\"background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0;\">"
                + "<p style=\"margin: 0;\"><strong>Sản phẩm:</strong> " + productName + "</p>"
                + "</div>"
                + "<div style=\"background: #f8f9fa; padding: 15px; border-left: 4px solid #00466a; margin: 20px 0;\">"
                + "<p style=\"margin: 0; font-size: 1em;\">" + message + "</p>"
                + "</div>"
                + "<p>Vui lòng đăng nhập vào hệ thống để xem chi tiết và thực hiện các hành động cần thiết.</p>"
                + "<p style=\"font-size: 0.9em;\">Trân trọng,<br />Đội ngũ CTU SHOP</p>"
                + "<hr style=\"border: none; border-top: 1px solid #eee;\" />"
                + "<div style=\"float: right; padding: 8px 0; color: #aaa; font-size: 0.8em; line-height: 1; font-weight: 300;\">"
                + "<p>CTU SHOP</p>"
                + "<p>Thành phố Cần Thơ</p>"
                + "<p>Email: support@fourstore.com</p>"
                + "</div>"
                + "</div>"
                + "</div>";
    }

    // Thêm vào EmailServiceImpl class
    @Override
    @Async
    public void sendInsuranceClaimNotification(String email, String fullName, String subject, String message, String claimCode) {
        try {
            logger.info("Processing insurance claim notification email to: " + email);
            String content = generateInsuranceClaimNotificationContent(fullName, subject, message, claimCode);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject(subject + " - CTU SHOP");
            helper.setText(content, true);
            mailSender.send(mimeMessage);
            logger.info("Insurance claim notification email sent successfully to: " + email);
        } catch (MessagingException e) {
            logger.error("Failed to send insurance claim notification email to: " + email + ", error: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendInsuranceClaimCommunication(String email, String fullName, String subject, String content, String claimCode) {
        try {
            logger.info("Processing insurance claim communication email to: " + email);
            String emailContent = generateInsuranceClaimCommunicationContent(fullName, subject, content, claimCode);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject(subject + " - CTU SHOP");
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
            logger.info("Insurance claim communication email sent successfully to: " + email);
        } catch (MessagingException e) {
            logger.error("Failed to send insurance claim communication email to: " + email + ", error: " + e.getMessage());
        }
    }

    private String generateInsuranceClaimNotificationContent(String fullName, String subject, String message, String claimCode) {
        return "<div style=\"font-family: Arial, sans-serif; min-width: 1000px; overflow: auto; line-height: 2;\">"
                + "<div style=\"margin: 50px auto; width: 70%; padding: 20px 0;\">"
                + "<div style=\"border-bottom: 1px solid #eee;\">"
                + "<a href=\"\" style=\"font-size: 1.4em; color: #00466a; text-decoration: none; font-weight: 600;\">CTU SHOP</a>"
                + "</div>"
                + "<p style=\"font-size: 1.1em;\">Xin chào, <b>" + fullName + "</b></p>"
                + "<h3 style=\"color: #00466a;\">" + subject + "</h3>"
                + "<div style=\"background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0;\">"
                + "<p style=\"margin: 0;\"><strong>Mã yêu cầu:</strong> " + claimCode + "</p>"
                + "</div>"
                + "<div style=\"background: #f8f9fa; padding: 15px; border-left: 4px solid #00466a; margin: 20px 0;\">"
                + "<p style=\"margin: 0; font-size: 1em;\">" + message + "</p>"
                + "</div>"
                + "<p>Để xem chi tiết và cập nhật yêu cầu, vui lòng đăng nhập vào hệ thống CTU Shop.</p>"
                + "<div style=\"text-align: center; margin: 30px 0;\">"
                + "<a href=\"#\" style=\"background: #00466a; color: white; padding: 12px 30px; text-decoration: none; border-radius: 4px; display: inline-block;\">Đăng nhập hệ thống</a>"
                + "</div>"
                + "<p style=\"font-size: 0.9em;\">Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>"
                + "<p style=\"font-size: 0.9em;\">Trân trọng,<br />Đội ngũ CTU SHOP</p>"
                + "<hr style=\"border: none; border-top: 1px solid #eee;\" />"
                + "<div style=\"float: right; padding: 8px 0; color: #aaa; font-size: 0.8em; line-height: 1; font-weight: 300;\">"
                + "<p>CTU SHOP - Hệ thống bảo hiểm sản phẩm</p>"
                + "<p>Thành phố Cần Thơ</p>"
                + "<p>Email: insurance@ctushop.com</p>"
                + "</div>"
                + "</div>"
                + "</div>";
    }

    private String generateInsuranceClaimCommunicationContent(String fullName, String subject, String content, String claimCode) {
        return "<div style=\"font-family: Arial, sans-serif; min-width: 1000px; overflow: auto; line-height: 2;\">"
                + "<div style=\"margin: 50px auto; width: 70%; padding: 20px 0;\">"
                + "<div style=\"border-bottom: 1px solid #eee;\">"
                + "<a href=\"\" style=\"font-size: 1.4em; color: #00466a; text-decoration: none; font-weight: 600;\">CTU SHOP</a>"
                + "</div>"
                + "<p style=\"font-size: 1.1em;\">Xin chào, <b>" + fullName + "</b></p>"
                + "<h3 style=\"color: #00466a;\">" + subject + "</h3>"
                + "<div style=\"background: #e7f3ff; padding: 15px; border-left: 4px solid #007bff; margin: 20px 0;\">"
                + "<p style=\"margin: 0;\"><strong>Liên quan đến yêu cầu:</strong> " + claimCode + "</p>"
                + "</div>"
                + "<div style=\"background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;\">"
                + "<p style=\"margin: 0; font-size: 1em; white-space: pre-line;\">" + content + "</p>"
                + "</div>"
                + "<p>Để phản hồi hoặc xem thêm chi tiết, vui lòng đăng nhập vào hệ thống CTU Shop.</p>"
                + "<div style=\"text-align: center; margin: 30px 0;\">"
                + "<a href=\"#\" style=\"background: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 4px; display: inline-block;\">Phản hồi ngay</a>"
                + "</div>"
                + "<p style=\"font-size: 0.9em;\">Trân trọng,<br />Đội ngũ hỗ trợ CTU SHOP</p>"
                + "<hr style=\"border: none; border-top: 1px solid #eee;\" />"
                + "<div style=\"float: right; padding: 8px 0; color: #aaa; font-size: 0.8em; line-height: 1; font-weight: 300;\">"
                + "<p>CTU SHOP - Hệ thống bảo hiểm sản phẩm</p>"
                + "<p>Hotline: 1900-xxxx</p>"
                + "<p>Email: support@ctushop.com</p>"
                + "</div>"
                + "</div>"
                + "</div>";
    }

}