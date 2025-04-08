package com.example.newswebsite.domain.response;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import com.example.newswebsite.util.ArticleStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO {
    private Integer articleId;
    private String title;
    private String summary;
    private String content;
    private String authorName;
    private Integer authorId;
    private String authorGoogleId;
    private String authorEmail;
    private String authorAvatar;
    private LocalDateTime createdAt;
    private ArticleStatus status;
    private CategoryDTO category;
    private Set<TagDTO> tags = Collections.emptySet(); // Mặc định là set rỗng
    private String thumbnailUrl;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDTO {
        private Integer categoryId;
        private String name;
        private String description;
        private Integer parentId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagDTO {
        private Integer tagId;
        private String name;
    }
}