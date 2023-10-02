package com.example.shoppingsystem.auth.interfaces;

import com.example.shoppingsystem.entities.AccessToken;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public interface JwtService {
    String extractUsername(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    String generateAccessToken(UserDetails userDetails);
    boolean isAccessTokenValid(AccessToken token, UserDetails userDetails);
    boolean isTokenExpired(AccessToken accessToken);
}
