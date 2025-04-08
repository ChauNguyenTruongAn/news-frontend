package com.example.newswebsite.domain.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho thông tin tóm tắt bài viết yêu thích
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteSummaryDTO {
    private Integer articleId;
    private String title;
    private String summary;
    private String thumbnailUrl;
    private String authorName;
    private String categoryName;
    private Integer categoryId;
    private LocalDateTime favoritedAt;
}