package com.example.newswebsite.domain.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thông tin thống kê của website
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebsiteStatsDTO {
    // Tổng số bài viết
    private long totalArticles;

    // Số bài viết theo trạng thái
    private Map<String, Long> articlesByStatus;

    // Số bài viết theo danh mục
    private Map<String, Long> articlesByCategory;

    // Tổng số người dùng
    private long totalUsers;

    // Số người dùng theo vai trò
    private Map<String, Long> usersByRole;

    // Tổng số bình luận
    private long totalComments;

    // Số bình luận theo tháng
    private List<MonthlyStats> commentsByMonth;

    // Tổng số lượt xem
    private long totalViews;

    // Số lượt xem theo tháng
    private List<MonthlyStats> viewsByMonth;

    // Tổng số bài viết yêu thích
    private long totalFavorites;

    // Bài viết phổ biến nhất (top 5)
    private List<PopularArticle> popularArticles;

    // Tác giả tích cực nhất (top 5)
    private List<ActiveAuthor> activeAuthors;

    // Thời gian cập nhật thống kê
    private LocalDateTime lastUpdated;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStats {
        private String month;
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularArticle {
        private Integer articleId;
        private String title;
        private long viewCount;
        private long commentCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveAuthor {
        private String googleId;
        private String name;
        private long articleCount;
        private long totalViews;
    }
}