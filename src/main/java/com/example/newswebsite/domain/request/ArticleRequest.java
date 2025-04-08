package com.example.newswebsite.domain.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRequest {
    private String title;
    private String summary;
    private String content;
    private Integer categoryId;
    private List<String> tagNames;
    private String thumbnailUrl;
}