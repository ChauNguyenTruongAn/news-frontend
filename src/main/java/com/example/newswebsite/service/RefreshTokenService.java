package com.example.newswebsite.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.newswebsite.domain.RefreshToken;
import com.example.newswebsite.domain.User;
import com.example.newswebsite.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtService jwtService;

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserUserId(user.getUserId());
        if (refreshToken != null) {
            refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
            refreshToken.setToken(jwtService.generateRefreshToken(user.getGoogleId()));
            return refreshTokenRepository.save(refreshToken);
        } else {
            refreshToken = new RefreshToken();
            refreshToken.setUser(user);
            refreshToken.setToken(jwtService.generateRefreshToken(user.getGoogleId()));
            refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7)); // 7 ngày
            return refreshTokenRepository.save(refreshToken);
        }
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean validateRefreshToken(String token) {
        Optional<RefreshToken> refreshToken = findByToken(token);
        return refreshToken.isPresent() && refreshToken.get().getExpiryDate().isAfter(LocalDateTime.now());
    }

    public void deleteByUserGoogleId(String googleId) {
        refreshTokenRepository.deleteByUserGoogleId(googleId);
    }
}