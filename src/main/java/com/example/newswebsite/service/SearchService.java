package com.example.newswebsite.service;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.newswebsite.domain.Article;
import com.example.newswebsite.domain.response.SearchResultDTO;
import com.example.newswebsite.repository.ArticleRepository;
import com.example.newswebsite.util.ArticleStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final ArticleRepository articleRepository;

    public Page<SearchResultDTO> searchArticles(String keyword, Pageable pageable) {
        log.info("Searching articles with keyword: {}", keyword);
        Page<Article> articles = articleRepository.searchArticles(
                keyword,
                ArticleStatus.PUBLISHED,
                pageable);

        return articles.map(this::convertToSearchResultDTO);
    }

    public Page<SearchResultDTO> searchArticlesByCategory(String keyword, Integer categoryId, Pageable pageable) {
        log.info("Searching articles with keyword: {} in category: {}", keyword, categoryId);
        Page<Article> articles = articleRepository.searchArticlesByCategory(
                keyword,
                ArticleStatus.PUBLISHED,
                categoryId,
                pageable);

        return articles.map(this::convertToSearchResultDTO);
    }

    private String prepareKeyword(String keyword) {
        // Thêm dấu * vào cuối mỗi từ để tìm kiếm prefix
        return keyword.trim().replaceAll("\\s+", "* *") + "*";
    }

    private SearchResultDTO convertToSearchResultDTO(Article article) {
        SearchResultDTO dto = new SearchResultDTO();
        dto.setArticleId(article.getArticleId());
        dto.setTitle(article.getTitle());
        dto.setSummary(article.getSummary());
        dto.setContent(article.getContent());
        dto.setAuthor(article.getAuthor());
        dto.setCategory(article.getCategory());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setThumbnailUrl(article.getThumbnailUrl());
        return dto;
    }
}