package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.AccessToken;
import com.example.shoppingsystem.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken,Long> {
    Optional<AccessToken> findByToken(String token);
    List<AccessToken> findAllByRefreshToken(RefreshToken token);
}
