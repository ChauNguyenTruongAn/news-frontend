package com.example.newswebsite.domain.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleUpdateRequest {
    private String title;
    private String summary;
    private String categoryName;
    private Integer categoryId;
    private List<String> tags;
    private String content;
    private String thumbnailUrl;
}