package com.example.newswebsite.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.newswebsite.domain.Article;
import com.example.newswebsite.domain.Favorite;
import com.example.newswebsite.domain.User;
import com.example.newswebsite.domain.request.FavoriteRequest;
import com.example.newswebsite.domain.response.FavoriteSummaryDTO;
import com.example.newswebsite.repository.ArticleRepository;
import com.example.newswebsite.repository.FavoriteRepository;
import com.example.newswebsite.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    public Favorite saveFavorite(FavoriteRequest request) throws Exception {
        Favorite favorite = new Favorite();
        User user = userRepository.findByGoogleId(request.getGoogleId()); // google id
        Article article = articleRepository.findById(request.getArticleId())
                .orElseThrow(() -> new Exception("Not found article"));

        if (user == null || article == null) {
            return null;
        }

        Favorite check = favoriteRepository
                .findByArticleArticleIdAndUserGoogleId(request.getArticleId(), request.getGoogleId())
                .orElse(null);
        if (check == null) {
            favorite.setUser(user);
            favorite.setArticle(article);
            favorite.setActive(true);
            favorite.setFavoritedAt(LocalDateTime.now());
        } else {
            check.setActive(!check.isActive());
            return favoriteRepository.save(check);
        }
        return favoriteRepository.save(favorite);
    }

    public Favorite toggleFavorite(Integer id) {
        String googleId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Favorite> optionalFavorite = favoriteRepository.findByFavoriteIdAndUserGoogleId(id, googleId);
        if (optionalFavorite.isPresent()) {
            Favorite favorite = optionalFavorite.get();
            favorite.setActive(!favorite.isActive()); // Bật/tắt
            favorite.setFavoritedAt(LocalDateTime.now());
            return favoriteRepository.save(favorite);
        }
        return null;
    }

    public List<Favorite> getUserFavorites() {
        String googleId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return favoriteRepository.findByUserGoogleIdOrderByFavoritedAtDesc(googleId);
    }

    /**
     * Lấy danh sách bài viết yêu thích của người dùng theo googleId
     * 
     * @param googleId ID Google của người dùng
     * @return Danh sách DTO chứa thông tin bài viết yêu thích
     */
    public List<FavoriteSummaryDTO> getUserFavoritesByGoogleId(String googleId) {
        log.info("Getting favorites for user with googleId: {}", googleId);

        List<Favorite> favorites = favoriteRepository.findAllByUserGoogleIdWithDetails(googleId);
        List<FavoriteSummaryDTO> result = new ArrayList<>();

        for (Favorite favorite : favorites) {
            try {
                Article article = favorite.getArticle();
                if (article != null) {
                    FavoriteSummaryDTO dto = new FavoriteSummaryDTO();
                    dto.setArticleId(article.getArticleId());
                    dto.setTitle(article.getTitle());
                    dto.setSummary(article.getSummary());
                    dto.setThumbnailUrl(article.getThumbnailUrl());
                    dto.setAuthorName(article.getAuthor() != null ? article.getAuthor().getName() : "");
                    dto.setCategoryName(article.getCategory() != null ? article.getCategory().getName() : "");
                    dto.setCategoryId(article.getCategory() != null ? article.getCategory().getCategoryId() : null);
                    dto.setFavoritedAt(favorite.getFavoritedAt());

                    result.add(dto);
                }
            } catch (Exception e) {
                log.error("Error processing article ID {}: {}", favorite.getArticle().getArticleId(), e.getMessage());
            }
        }

        return result;
    }

    /**
     * Kiểm tra xem một bài viết có được yêu thích bởi người dùng không
     * 
     * @param articleId ID của bài viết
     * @param googleId  ID Google của người dùng
     * @return true nếu bài viết được yêu thích, false nếu không
     */
    public boolean isArticleFavoritedByUser(Integer articleId, String googleId) {
        return favoriteRepository.findByArticleArticleIdAndUserGoogleId(articleId, googleId).isPresent();
    }

    public boolean deleteFavorite(Integer id) {
        String googleId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Favorite> optionalFavorite = favoriteRepository.findByFavoriteIdAndUserGoogleId(id, googleId);
        if (optionalFavorite.isPresent()) {
            favoriteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
