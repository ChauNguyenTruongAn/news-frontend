package com.example.newswebsite.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.newswebsite.domain.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);

    RefreshToken findByUserUserId(Integer id);

    void deleteByUserGoogleId(String googleId);
}
