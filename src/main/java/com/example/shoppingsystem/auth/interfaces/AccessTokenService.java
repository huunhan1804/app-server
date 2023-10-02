package com.example.shoppingsystem.auth.interfaces;

import com.example.shoppingsystem.entities.AccessToken;
import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.RefreshToken;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface AccessTokenService {
    Optional<AccessToken> findByToken(String token);
    AccessToken createToken(Account account, RefreshToken refreshToken);
    boolean deleteToken(AccessToken accessToken);
}
