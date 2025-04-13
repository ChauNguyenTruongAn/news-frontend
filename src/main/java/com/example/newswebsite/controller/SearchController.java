package com.example.newswebsite.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.newswebsite.domain.response.SearchResultDTO;
import com.example.newswebsite.service.SearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "API Tìm kiếm", description = "API tìm kiếm bài viết")
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    @Operation(summary = "Tìm kiếm bài viết", description = "Tìm kiếm bài viết theo từ khóa")
    public ResponseEntity<Page<SearchResultDTO>> searchArticles(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam String keyword,
            Pageable pageable) {
        log.info("Search request received with keyword: {}", keyword);
        return ResponseEntity.ok(searchService.searchArticles(keyword, pageable));
    }

    @GetMapping("/category")
    @Operation(summary = "Tìm kiếm bài viết theo danh mục", description = "Tìm kiếm bài viết theo từ khóa trong một danh mục cụ thể")
    public ResponseEntity<Page<SearchResultDTO>> searchArticlesByCategory(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam String keyword,
            @Parameter(description = "ID danh mục") @RequestParam Integer categoryId,
            Pageable pageable) {
        log.info("Search request received with keyword: {} in category: {}", keyword, categoryId);
        return ResponseEntity.ok(searchService.searchArticlesByCategory(keyword, categoryId, pageable));
    }
}