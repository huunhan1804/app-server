package com.example.shoppingsystem.auth;

import com.example.shoppingsystem.auth.interfaces.RefreshTokenService;
import com.example.shoppingsystem.constants.LogMessage;
import com.example.shoppingsystem.constants.Regex;
import com.example.shoppingsystem.entities.AccessToken;
import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.RefreshToken;
import com.example.shoppingsystem.repositories.AccessTokenRepository;
import com.example.shoppingsystem.repositories.RefreshTokenRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private static final Logger logger = LogManager.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenRepository accessTokenRepository;

    @Autowired
    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, AccessTokenRepository accessTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessTokenRepository = accessTokenRepository;
    }


    @Override
    public RefreshToken createToken(Account account) {
        logger.info(String.format(LogMessage.LOG_PROCESSING_CREATE_REFRESH_TOKEN, account.getAccountId()));

        List<RefreshToken> refreshTokenList = refreshTokenRepository.findAllByAccount(account);
        refreshTokenList.sort(Comparator.comparing(RefreshToken::getExpiryDate));
        if (refreshTokenList.size() >= 3) {
            List<AccessToken> accessTokenList = accessTokenRepository.findAllByRefreshToken(refreshTokenList.get(0));
            for (AccessToken accessToken : accessTokenList) {
                accessTokenRepository.delete(accessToken);
            }
            refreshTokenRepository.delete(refreshTokenList.get(0));
            logger.info(String.format(LogMessage.LOG_DELETE_REFRESH_TOKEN_EXPIRED, refreshTokenList.get(0).getAccount().getAccountId()));
        }
        LocalDateTime expiryDate = LocalDateTime.now().plusNanos(Regex.REFRESH_TOKEN_EXPIRATION_TIME);
        RefreshToken refreshToken = RefreshToken.builder()
                .account(account)
                .token(UUID.randomUUID().toString())
                .expiryDate(expiryDate)
                .build();
        refreshTokenRepository.save(refreshToken);

        logger.info(String.format(LogMessage.LOG_CREATE_REFRESH_TOKEN_SUCCESS, account.getAccountId()));
        return refreshToken;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        logger.info(String.format(LogMessage.LOG_FINDING_REFRESH_TOKEN, token));
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public boolean verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(LocalDateTime.now()) < 0) {
            List<AccessToken> accessTokenList = accessTokenRepository.findAllByRefreshToken(token);
            for (AccessToken accessToken : accessTokenList) {
                accessTokenRepository.delete(accessToken);
            }
            refreshTokenRepository.delete(token);
            logger.info(String.format(LogMessage.LOG_DELETE_REFRESH_TOKEN_EXPIRED, token.getAccount().getAccountId()));
            return false;
        }
        return true;
    }
}
