package com.example.newswebsite.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.newswebsite.domain.Article;
import com.example.newswebsite.domain.Comment;
import com.example.newswebsite.domain.Favorite;
import com.example.newswebsite.domain.ReadingHistory;
import com.example.newswebsite.domain.User;
import com.example.newswebsite.domain.response.WebsiteStatsDTO;
import com.example.newswebsite.domain.response.WebsiteStatsDTO.ActiveAuthor;
import com.example.newswebsite.domain.response.WebsiteStatsDTO.MonthlyStats;
import com.example.newswebsite.domain.response.WebsiteStatsDTO.PopularArticle;
import com.example.newswebsite.repository.ArticleRepository;
import com.example.newswebsite.repository.CommentRepository;
import com.example.newswebsite.repository.FavoriteRepository;
import com.example.newswebsite.repository.ReadingHistoryRepository;
import com.example.newswebsite.repository.UserRepository;
import com.example.newswebsite.util.ArticleStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final FavoriteRepository favoriteRepository;

    public WebsiteStatsDTO getWebsiteStats() {
        log.info("Getting website statistics");

        WebsiteStatsDTO stats = new WebsiteStatsDTO();

        // Thống kê bài viết
        stats.setTotalArticles(articleRepository.count());
        stats.setArticlesByStatus(getArticlesByStatus());
        stats.setArticlesByCategory(getArticlesByCategory());

        // Thống kê người dùng
        stats.setTotalUsers(userRepository.count());
        stats.setUsersByRole(getUsersByRole());

        // Thống kê bình luận
        stats.setTotalComments(commentRepository.count());
        stats.setCommentsByMonth(getCommentsByMonth());

        // Thống kê lượt xem
        stats.setTotalViews(readingHistoryRepository.count());
        stats.setViewsByMonth(getViewsByMonth());

        // Thống kê yêu thích
        stats.setTotalFavorites(favoriteRepository.count());

        // Bài viết phổ biến
        stats.setPopularArticles(getPopularArticles());

        // Tác giả tích cực
        stats.setActiveAuthors(getActiveAuthors());

        // Thời gian cập nhật
        stats.setLastUpdated(LocalDateTime.now());

        return stats;
    }

    private Map<String, Long> getArticlesByStatus() {
        Map<String, Long> result = new HashMap<>();
        for (ArticleStatus status : ArticleStatus.values()) {
            long count = articleRepository.countByStatus(status);
            result.put(status.name(), count);
        }
        return result;
    }

    private Map<String, Long> getArticlesByCategory() {
        return articleRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        article -> article.getCategory() != null ? article.getCategory().getName() : "Uncategorized",
                        Collectors.counting()));
    }

    private Map<String, Long> getUsersByRole() {
        return userRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        user -> user.getRole().getRoleName(),
                        Collectors.counting()));
    }

    private List<MonthlyStats> getCommentsByMonth() {
        return commentRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new MonthlyStats(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private List<MonthlyStats> getViewsByMonth() {
        return readingHistoryRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        view -> view.getViewedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new MonthlyStats(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private List<PopularArticle> getPopularArticles() {
        return articleRepository.findAll().stream()
                .map(article -> {
                    long viewCount = readingHistoryRepository.countByArticleArticleId(article.getArticleId());
                    long commentCount = commentRepository.countByArticleArticleId(article.getArticleId());
                    return new PopularArticle(
                            article.getArticleId(),
                            article.getTitle(),
                            viewCount,
                            commentCount);
                })
                .sorted((a1, a2) -> Long.compare(a2.getViewCount(), a1.getViewCount()))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<ActiveAuthor> getActiveAuthors() {
        return userRepository.findAll().stream()
                .map(author -> {
                    long articleCount = articleRepository.countByAuthorGoogleId(author.getGoogleId());
                    long totalViews = articleRepository.findByAuthorGoogleId(author.getGoogleId()).stream()
                            .mapToLong(
                                    article -> readingHistoryRepository.countByArticleArticleId(article.getArticleId()))
                            .sum();
                    return new ActiveAuthor(
                            author.getGoogleId(),
                            author.getName(),
                            articleCount,
                            totalViews);
                })
                .sorted((a1, a2) -> Long.compare(a2.getTotalViews(), a1.getTotalViews()))
                .limit(5)
                .collect(Collectors.toList());
    }
}