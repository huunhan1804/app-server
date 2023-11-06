package com.example.shoppingsystem.auth;

import com.example.shoppingsystem.auth.interfaces.OtpService;
import com.example.shoppingsystem.constants.Regex;
import com.example.shoppingsystem.entities.Otp;
import com.example.shoppingsystem.enums.OtpPurpose;
import com.example.shoppingsystem.extensions.OTPUtil;
import com.example.shoppingsystem.repositories.OtpRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class OtpServiceImpl implements OtpService {
    private static final Logger logger = LogManager.getLogger(OtpServiceImpl.class);
    private final OtpRepository otpRepository;

    @Autowired
    public OtpServiceImpl(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    @Override
    public Otp createOtp(String loginId, OtpPurpose otpPurpose) {
        Otp otp = Otp.builder()
                .loginId(loginId)
                .otpCode(generateOtpCode())
                .otpPurpose(otpPurpose)
                .expiryDate(calculateExpiryDate())
                .build();
        return otpRepository.save(otp);
    }

    @Override
    public boolean isOtpValid(String otpCode,String loginId, OtpPurpose otpPurpose) {
        Optional<Otp> otp = getOtpByLoginIdAndPurpose(loginId,otpPurpose);
        if (otp.isPresent() && otp.get().getOtpCode().equals(otpCode)) {
            return !otp.get().getExpiryDate().isBefore(LocalDateTime.now());
        }
        return false;
    }

    @Async
    @Override
    public void deleteOtp(Otp otp) {
        otpRepository.delete(otp);
    }

    @Override
    public Optional<Otp> getOtpByLoginIdAndPurpose(String loginId, OtpPurpose otpPurpose) {
        return otpRepository.findByLoginIdAndOtpPurpose(loginId, otpPurpose);
    }

    @Override
    public boolean canRequestOtp(String loginId, OtpPurpose otpPurpose) {
        Optional<Otp> otp = getOtpByLoginIdAndPurpose(loginId, otpPurpose);
        if (otp.isEmpty()) {
            return true;
        } else {
            LocalDateTime lastOtpRequestTime = otp.get().getCreatedDate();
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime minRequestInterval = lastOtpRequestTime.plusMinutes(Regex.DELAY_BETWEEN_REQUEST_SEND_OTP);
            return currentTime.isAfter(minRequestInterval);
        }
    }

    private String generateOtpCode() {return OTPUtil.generateOTP(Regex.LENGTH_CODE_OTP); }

    private LocalDateTime calculateExpiryDate() {return LocalDateTime.now().plusMinutes(5);}

    @Transactional
    @Scheduled(fixedRate = 300_000)
    public void cleanupExpiredOtps() {
        logger.info("Starting OTP cleanup...");
        LocalDateTime currentTime = LocalDateTime.now();
        otpRepository.deleteAllByExpiryDate(currentTime);
        logger.info("OTP cleanup completed.");
    }

}
