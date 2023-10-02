package com.example.shoppingsystem.config;

import com.example.shoppingsystem.auth.interfaces.JwtService;
import com.example.shoppingsystem.constants.ErrorCode;
import com.example.shoppingsystem.constants.LogMessage;
import com.example.shoppingsystem.constants.Message;
import com.example.shoppingsystem.entities.AccessToken;
import com.example.shoppingsystem.repositories.AccessTokenRepository;
import com.example.shoppingsystem.responses.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LogManager.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AccessTokenRepository accessTokenRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws IOException {
        final String authHeader = request.getHeader("Authorization");
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                final String jwt = authHeader.substring(7);
                final String loginId = jwtService.extractUsername(jwt);

                if (loginId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);
                    Optional<AccessToken> accessToken = accessTokenRepository.findByToken(jwt);

                    if (accessToken.isPresent() && jwtService.isAccessTokenValid(accessToken.get(), userDetails)) {
                        logger.info(String.format(LogMessage.LOG_TOKEN_VALID, loginId));
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.info(String.format(LogMessage.LOG_EMAIL_ACTION_REQUEST, loginId));
                    } else {
                        handleUnauthorized(response);
                        return;
                    }
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error(String.format(LogMessage.LOG_ERROR_OCCURRED, e.getMessage()));
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .status(ErrorCode.INTERNAL_SERVER_ERROR)
                    .message(e.getMessage())
                    .build();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(convertObjectToJson(apiResponse));
        }
    }

    private void handleUnauthorized(HttpServletResponse response) throws IOException {
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .status(ErrorCode.UNAUTHORIZED)
                .message(Message.TOKEN_INVALID)
                .timestamp(new Date())
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(convertObjectToJson(apiResponse));
    }

    private String convertObjectToJson(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }
}
