package com.example.newswebsite.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.newswebsite.domain.ReadingHistory;

@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Integer> {
    List<ReadingHistory> findByUserGoogleId(String googleId);

    Optional<ReadingHistory> findByHistoryIdAndUserGoogleId(Integer id, String googleId);

    List<ReadingHistory> findByUserGoogleIdOrderByViewedAtDesc(String googleId);

    @Query("SELECT rh FROM ReadingHistory rh WHERE rh.user.googleId = :googleId ORDER BY rh.viewedAt DESC")
    List<ReadingHistory> findAllByUserGoogleIdWithDetails(@Param("googleId") String googleId);

    /**
     * Đếm số lần đọc mỗi bài viết của một người dùng và lấy thời gian đọc gần nhất
     * 
     * @param googleId ID Google của người dùng
     * @return Danh sách mảng Object chứa các thông tin: [articleId, số lần đọc,
     *         thời gian đọc gần nhất]
     */
    @Query("SELECT rh.article.articleId, COUNT(rh), MAX(rh.viewedAt) FROM ReadingHistory rh " +
            "WHERE rh.user.googleId = :googleId " +
            "GROUP BY rh.article.articleId " +
            "ORDER BY MAX(rh.viewedAt) DESC")
    List<Object[]> countViewsByArticleAndUserGoogleId(@Param("googleId") String googleId);

    @Query("SELECT COUNT(rh) FROM ReadingHistory rh WHERE rh.article.articleId = :articleId")
    long countByArticleArticleId(@Param("articleId") Integer articleId);
}
