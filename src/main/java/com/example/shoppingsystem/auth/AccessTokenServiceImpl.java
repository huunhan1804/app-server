package com.example.shoppingsystem.auth;

import com.example.shoppingsystem.auth.interfaces.AccessTokenService;
import com.example.shoppingsystem.auth.interfaces.JwtService;
import com.example.shoppingsystem.constants.LogMessage;
import com.example.shoppingsystem.constants.Regex;
import com.example.shoppingsystem.entities.AccessToken;
import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.RefreshToken;
import com.example.shoppingsystem.repositories.AccessTokenRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class AccessTokenServiceImpl implements AccessTokenService {
    private static final Logger logger = LogManager.getLogger(AccessTokenServiceImpl.class);
    private final AccessTokenRepository accessTokenRepository;
    private final JwtService jwtService;

    @Autowired
    public AccessTokenServiceImpl(AccessTokenRepository accessTokenRepository, JwtService jwtService) {
        this.accessTokenRepository = accessTokenRepository;
        this.jwtService = jwtService;
    }

    @Override
    public Optional<AccessToken> findByToken(String token) {
        return accessTokenRepository.findByToken(token);
    }

    @Override
    public AccessToken createToken(Account account, RefreshToken refreshToken) {
        logger.info(String.format(LogMessage.LOG_PROCESSING_CREATE_ACCESS_TOKEN, account.getAccountId()));

        List<AccessToken> accessTokenList = accessTokenRepository.findAllByRefreshToken(refreshToken);
        List<AccessToken> validAccessTokenList = new ArrayList<>();

        for (AccessToken accessToken : accessTokenList) {
            if (verifyExpiration(accessToken)) {
                validAccessTokenList.add(accessToken);
            }
        }

        if (validAccessTokenList.size() < 3) {
            String accessTokenValue = jwtService.generateAccessToken(account);
            LocalDateTime expiryDate = LocalDateTime.now().plusNanos(Regex.ACCESS_TOKEN_EXPIRATION_TIME);
            AccessToken accessToken = AccessToken.builder()
                    .account(account)
                    .token(accessTokenValue)
                    .expiryDate(expiryDate)
                    .refreshToken(refreshToken)
                    .build();

            accessTokenRepository.save(accessToken);

            logger.info(String.format(LogMessage.LOG_CREATE_ACCESS_TOKEN_SUCCESS, account.getAccountId()));
            return accessToken;
        } else {
            logger.error(String.format(LogMessage.LOG_CREATE_ACCESS_TOKEN_FAILED, account.getAccountId()));
            return null;
        }
    }

    @Override
    public boolean deleteToken(AccessToken accessToken) {
        try {
            accessTokenRepository.delete(accessToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean verifyExpiration(AccessToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            logger.info(String.format(LogMessage.LOG_DELETE_ACCESS_TOKEN_EXPIRED, token.getAccount().getAccountId()));
            accessTokenRepository.delete(token);
            return false;
        }
        return true;
    }

}
