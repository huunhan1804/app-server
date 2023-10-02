package com.example.shoppingsystem.config;

import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.LogMessage;
import com.example.shoppingsystem.entities.AccessToken;
import com.example.shoppingsystem.entities.RefreshToken;
import com.example.shoppingsystem.repositories.AccessTokenRepository;
import com.example.shoppingsystem.repositories.RefreshTokenRepository;
import com.example.shoppingsystem.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {
    private static final Logger logger = LogManager.getLogger(LogoutService.class);
    private final AccessTokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        logger.info(LogMessage.LOG_RECEIVED_LOGOUT_REQUEST);

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.info(LogMessage.LOG_EMPTY_OR_INVALID_JWT_HEADER);
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.builder().status(ErrorCode.FORBIDDEN).message(LogMessage.LOG_EMPTY_OR_INVALID_JWT_HEADER).build());
            return;
        }
        jwt = authHeader.substring(7);
        try {
            Optional<AccessToken> storedToken = tokenRepository.findByToken(jwt);
            if (storedToken.isPresent()) {
                Optional<RefreshToken> refreshToken = refreshTokenRepository.findByRefreshTokenId(storedToken.get().getRefreshToken().getRefreshTokenId());
                tokenRepository.delete(storedToken.get());
                if (refreshToken.isPresent()) {
                    List<AccessToken> accessTokenList = tokenRepository.findAllByRefreshToken(refreshToken.get());
                    logger.info(String.format(LogMessage.LOG_DELETING_ACCESS_TOKENS, refreshToken.get().getRefreshTokenId()));
                    for (AccessToken accessToken : accessTokenList) {
                        tokenRepository.delete(accessToken);
                    }
                    refreshTokenRepository.delete(refreshToken.get());
                    logger.info(String.format(LogMessage.LOG_DELETING_REFRESH_TOKEN, refreshToken.get().getRefreshTokenId()));
                }
            }

            SecurityContextHolder.clearContext();
            logger.info(LogMessage.LOG_LOGOUT_SUCCESSFUL);
            ResponseEntity.ok(ApiResponse.builder().status(ErrorCode.SUCCESS).message(LogMessage.LOG_LOGOUT_SUCCESSFUL).build());
        } catch (Exception e) {
            logger.error(String.format(LogMessage.LOG_FAILED_TO_LOGOUT, e.getMessage()));
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.builder().status(ErrorCode.SUCCESS).message(LogMessage.LOG_TOKEN_NOT_FOUND).build());
        }
    }

}

