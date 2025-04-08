package com.example.newswebsite.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.example.newswebsite.domain.Article;
import com.example.newswebsite.util.ArticleStatus;

import jakarta.persistence.criteria.Predicate;

public class ArticleSpecification {
    public static Specification<Article> searchArticles(String title, Integer categoryId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Luôn chỉ lấy bài viết đã xuất bản (PUBLISHED)
            predicates.add(cb.equal(root.get("status"), ArticleStatus.PUBLISHED));

            if (title != null && !title.isEmpty()) {
                predicates.add(cb.like(root.get("title"), "%" + title + "%"));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("categoryId"), categoryId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Tạo Specification cho tìm kiếm bài viết với khả năng chỉ định trạng thái
     * 
     * @param title      Tiêu đề bài viết (tùy chọn)
     * @param categoryId ID của danh mục (tùy chọn)
     * @param status     Trạng thái bài viết (tùy chọn)
     * @return Specification để tìm kiếm bài viết
     */
    public static Specification<Article> searchArticlesWithStatus(String title, Integer categoryId,
            ArticleStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (title != null && !title.isEmpty()) {
                predicates.add(cb.like(root.get("title"), "%" + title + "%"));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("categoryId"), categoryId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
