package com.example.newswebsite.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.newswebsite.domain.Article;
import com.example.newswebsite.domain.ReadingHistory;
import com.example.newswebsite.domain.User;
import com.example.newswebsite.domain.response.ReadingHistorySummaryDTO;
import com.example.newswebsite.repository.ArticleRepository;
import com.example.newswebsite.repository.ReadingHistoryRepository;
import com.example.newswebsite.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReadingHistoryService {

    private final ReadingHistoryRepository readingHistoryRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    public ReadingHistory saveReadingHistory(ReadingHistory history) {
        String googleId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByGoogleId(googleId);
        history.setUser(user);
        history.setViewedAt(LocalDateTime.now());
        return readingHistoryRepository.save(history);
    }

    public List<ReadingHistory> getUserReadingHistory() {
        String googleId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return readingHistoryRepository.findByUserGoogleIdOrderByViewedAtDesc(googleId);
    }

    /**
     * Lấy lịch sử đọc của người dùng theo googleId, gom nhóm theo bài viết và hiển
     * thị số lần đọc
     * 
     * @param googleId Google ID của người dùng
     * @return Danh sách DTO chứa thông tin bài viết và số lần đọc
     */
    public List<ReadingHistorySummaryDTO> getUserReadingHistoryByGoogleId(String googleId) {
        log.info("Getting reading history for user with googleId: {}", googleId);

        // Lấy thông tin số lần đọc và thời gian đọc gần nhất
        List<Object[]> viewCounts = readingHistoryRepository.countViewsByArticleAndUserGoogleId(googleId);

        // Tạo map articleId -> số lần đọc để dễ truy xuất
        Map<Integer, Object[]> articleViewsMap = viewCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0], // articleId
                        row -> row));

        // Kết quả trả về
        List<ReadingHistorySummaryDTO> result = new ArrayList<>();

        // Lấy chi tiết bài viết và tạo DTO
        for (Integer articleId : articleViewsMap.keySet()) {
            try {
                Article article = articleRepository.findById(articleId).orElse(null);
                if (article != null) {
                    Object[] viewData = articleViewsMap.get(articleId);
                    long count = (long) viewData[1];
                    LocalDateTime lastViewed = (LocalDateTime) viewData[2];

                    ReadingHistorySummaryDTO dto = new ReadingHistorySummaryDTO();
                    dto.setArticleId(articleId);
                    dto.setTitle(article.getTitle());
                    dto.setSummary(article.getSummary());
                    dto.setThumbnailUrl(article.getThumbnailUrl());
                    dto.setAuthorName(article.getAuthor() != null ? article.getAuthor().getName() : "");
                    dto.setCategoryName(article.getCategory() != null ? article.getCategory().getName() : "");
                    dto.setCategoryId(article.getCategory() != null ? article.getCategory().getCategoryId() : null);
                    dto.setViewCount(count);
                    dto.setLastViewedAt(lastViewed);

                    result.add(dto);
                }
            } catch (Exception e) {
                log.error("Error processing article ID {}: {}", articleId, e.getMessage());
            }
        }

        // Sắp xếp theo thời gian đọc gần nhất
        result.sort((a, b) -> b.getLastViewedAt().compareTo(a.getLastViewedAt()));

        return result;
    }

    public boolean deleteReadingHistory(Integer id) {
        String googleId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<ReadingHistory> optionalHistory = readingHistoryRepository.findByHistoryIdAndUserGoogleId(id,
                googleId);
        if (optionalHistory.isPresent()) {
            readingHistoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}