package com.example.shoppingsystem.auth;

import com.example.shoppingsystem.auth.interfaces.EmailService;
import com.example.shoppingsystem.constants.LogMessage;
import com.example.shoppingsystem.constants.Regex;
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
            helper.setSubject(Regex.SUBJECT_EMAIL_PASSWORD);
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
            helper.setSubject(Regex.SUBJECT_EMAIL_OTP);
            helper.setText(content, true);
            mailSender.send(message);
            logger.info(String.format(LogMessage.LOG_SEND_EMAIL_SUCCESS, email));
            logger.info(LogMessage.LOG_SEND_EMAIL_DONE);
        } catch (MessagingException e) {
            logger.error(String.format(LogMessage.LOG_SEND_EMAIL_FAILED, email, e.getMessage()));
        }
    }

    private String generatePasswordRecoveryContent(String password, String email) {
        return "<div style=\"font-family: Arial, sans-serif; min-width: 1000px; overflow: auto; line-height: 2;\">"
                + "<div style=\"margin: 50px auto; width: 70%; padding: 20px 0;\">"
                + "<div style=\"border-bottom: 1px solid #eee;\">"
                + "<a href=\"\" style=\"font-size: 1.4em; color: #00466a; text-decoration: none; font-weight: 600;\">Four Store</a>"
                + "</div>"
                + "<p style=\"font-size: 1.1em;\">Hello, <b>" + email + "</b></p>"
                + "<p>Thank you for choosing Four Store for your shopping experience. Please use the following password to reset your password.</p>"
                + "<h2 style=\"background: #00466a; margin: 0 auto; width: max-content; padding: 0 10px; color: #fff; border-radius: 4px;\">"
                + password
                + "</h2>"
                + "<p style=\"font-size: 0.9em;\">Best regards,<br />Four Store</p>"
                + "<hr style=\"border: none; border-top: 1px solid #eee;\" />"
                + "<div style=\"float: right; padding: 8px 0; color: #aaa; font-size: 0.8em; line-height: 1; font-weight: 300;\">"
                + "<p>Four Store</p>"
                + "<p>Can Tho City</p>"
                + "</div>"
                + "</div>"
                + "</div>";
    }

    private String generateOtpContent(String otp, String email) {
        return "<div style=\"font-family: Arial, sans-serif; min-width: 1000px; overflow: auto; line-height: 2;\">"
                + "<div style=\"margin: 50px auto; width: 70%; padding: 20px 0;\">"
                + "<div style=\"border-bottom: 1px solid #eee;\">"
                + "<a href=\"\" style=\"font-size: 1.4em; color: #00466a; text-decoration: none; font-weight: 600;\">Four Store</a>"
                + "</div>"
                + "<p style=\"font-size: 1.1em;\">Hello, <b>" + email + "</b></p>"
                + "<p>Thank you for choosing Four Store for your shopping experience. Please use the following otp to confirm account and continuous.</p>"
                + "<h2 style=\"background: #00466a; margin: 0 auto; width: max-content; padding: 0 10px; color: #fff; border-radius: 4px;\">"
                + otp
                + "</h2>"
                + "<p style=\"font-size: 0.9em;\">Best regards,<br />Four Store</p>"
                + "<hr style=\"border: none; border-top: 1px solid #eee;\" />"
                + "<div style=\"float: right; padding: 8px 0; color: #aaa; font-size: 0.8em; line-height: 1; font-weight: 300;\">"
                + "<p>Four Store</p>"
                + "<p>Can Tho City</p>"
                + "</div>"
                + "</div>"
                + "</div>";
    }

}
