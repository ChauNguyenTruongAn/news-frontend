package com.example.newswebsite.domain.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho thông tin tóm tắt lịch sử đọc, gộp nhiều lần đọc một bài viết thành
 * một mục
 * với số lần đọc và thời gian đọc gần nhất
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadingHistorySummaryDTO {
    private Integer articleId;
    private String title;
    private String summary;
    private String thumbnailUrl;
    private String authorName;
    private String categoryName;
    private Integer categoryId;
    private long viewCount; // Số lần đọc bài viết
    private LocalDateTime lastViewedAt; // Thời gian đọc gần nhất
}