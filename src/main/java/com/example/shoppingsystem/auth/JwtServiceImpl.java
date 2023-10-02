package com.example.shoppingsystem.auth;

import com.example.shoppingsystem.auth.interfaces.JwtService;
import com.example.shoppingsystem.constants.LogMessage;
import com.example.shoppingsystem.constants.Regex;
import com.example.shoppingsystem.entities.AccessToken;
import com.example.shoppingsystem.repositories.AccessTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtServiceImpl implements JwtService {
    private static final Logger logger = LogManager.getLogger(JwtServiceImpl.class);
    private final AccessTokenRepository accessTokenRepository;

    @Autowired
    public JwtServiceImpl(AccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    @Override
    public String extractUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (Exception ex) {
            logger.error(String.format(LogMessage.LOG_ERROR_EXTRACTING_USERNAME, ex.getMessage()));
            return null;
        }
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    @Override
    public boolean isAccessTokenValid(AccessToken accessToken, UserDetails userDetails) {
        final String username = extractUsername(accessToken.getToken());
        boolean isTokenExpired = isTokenExpired(accessToken);
        if (isTokenExpired) {
            accessTokenRepository.delete(accessToken);
            logger.info(String.format(LogMessage.LOG_ACCESS_TOKEN_EXPIRED, username));
        }
        boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired;
        if (!isValid) {
            logger.info(String.format(LogMessage.LOG_ACCESS_TOKEN_INVALID, accessToken));
        }
        return isValid;
    }

    @Override
    public boolean isTokenExpired(AccessToken accessToken) {
        boolean isExpired = extractExpiration(accessToken.getToken()).before(new Date()) || accessToken.getExpiryDate().isBefore(LocalDateTime.now());
        if (isExpired) {
            logger.warn(String.format(LogMessage.LOG_ACCESS_TOKEN_EXPIRED, accessToken.getAccount().getEmail()));
        }
        return isExpired;
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTime = now.plusNanos(Regex.ACCESS_TOKEN_EXPIRATION_TIME);

        Date issuedAt = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        Date expiration = Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(Regex.SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
