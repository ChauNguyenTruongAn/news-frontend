package com.example.newswebsite.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.newswebsite.domain.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    List<Favorite> findByUserGoogleId(String googleId);

    Optional<Favorite> findByFavoriteIdAndUserGoogleId(Integer id, String googleId);

    Optional<Favorite> findByArticleArticleIdAndUserGoogleId(Integer articleId, String googleId);

    @Query("SELECT f FROM Favorite f WHERE f.user.googleId = :googleId ORDER BY f.favoritedAt DESC")
    List<Favorite> findAllByUserGoogleIdWithDetails(@Param("googleId") String googleId);

    List<Favorite> findByUserGoogleIdOrderByFavoritedAtDesc(String googleId);
}