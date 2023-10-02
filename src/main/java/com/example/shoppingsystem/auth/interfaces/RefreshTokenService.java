package com.example.shoppingsystem.auth.interfaces;

import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.RefreshToken;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface RefreshTokenService{
    RefreshToken createToken(Account account);
    Optional<RefreshToken> findByToken(String token);
    boolean verifyExpiration(RefreshToken token);
}
