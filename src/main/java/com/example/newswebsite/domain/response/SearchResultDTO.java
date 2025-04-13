package com.example.newswebsite.domain.response;

import java.time.LocalDateTime;

import com.example.newswebsite.domain.Category;
import com.example.newswebsite.domain.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO {
    private Integer articleId;
    private String title;
    private String summary;
    private String content;
    private User author;
    private Category category;
    private LocalDateTime createdAt;
    private String thumbnailUrl;
    private double relevanceScore;
}