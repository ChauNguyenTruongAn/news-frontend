package com.example.newswebsite.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.newswebsite.domain.Article;
import com.example.newswebsite.service.ArticleService;
import com.example.newswebsite.util.ArticleStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
@Slf4j
public class AdminArticleController {

    private final ArticleService articleService;
    @PutMapping("/{articleId}/unpublish")
    public ResponseEntity<Article> unpublishArticle(@PathVariable Integer articleId) {
        log.info("Unpublishing article {}", articleId);
        Article article = articleService.updateArticleStatus(articleId, ArticleStatus.PENDING);
        return ResponseEntity.ok(article);
    }
}