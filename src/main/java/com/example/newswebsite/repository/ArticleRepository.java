package com.example.newswebsite.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.newswebsite.domain.Article;
import com.example.newswebsite.domain.Category;
import com.example.newswebsite.domain.User;
import com.example.newswebsite.util.ArticleStatus;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer>, JpaSpecificationExecutor<Article> {
        Page<Article> findAll(Specification<Article> spec, Pageable pageable);

        @EntityGraph(attributePaths = { "author", "category", "tags" })
        Page<Article> findByStatus(ArticleStatus status, Pageable pageable);

        @EntityGraph(attributePaths = { "author", "category", "tags" })
        Page<Article> findByCategoryCategoryIdAndStatus(Integer categoryId, ArticleStatus status, Pageable pageable);

        @EntityGraph(attributePaths = { "author", "category", "tags" })
        Page<Article> findByAuthorGoogleId(String googleId, Pageable pageable);

        Page<Article> findByCategoryAndStatus(Category category, ArticleStatus status, Pageable pageable);

        Page<Article> findByAuthorAndStatus(User author, ArticleStatus status, Pageable pageable);

        Page<Article> findByTitleContainingIgnoreCaseAndStatus(String title, ArticleStatus status, Pageable pageable);

        Page<Article> findByCategoryAndTitleContainingIgnoreCaseAndStatus(
                        Category category, String title, ArticleStatus status, Pageable pageable);

        Page<Article> findByAuthorAndTitleContainingIgnoreCaseAndStatus(
                        User author, String title, ArticleStatus status, Pageable pageable);

        @Query("SELECT a FROM Article a WHERE a.author.googleId = :googleId AND a.status = :status")
        Page<Article> findByAuthorGoogleIdAndStatus(
                        @Param("googleId") String googleId,
                        @Param("status") ArticleStatus status,
                        Pageable pageable);

        @Query("SELECT a FROM Article a WHERE a.author.googleId = :googleId")
        List<Article> findByAuthorGoogleId(@Param("googleId") String googleId);

        @Query("SELECT COUNT(a) FROM Article a WHERE a.author.googleId = :googleId")
        long countByAuthorGoogleId(@Param("googleId") String googleId);

        @Query("SELECT COUNT(a) FROM Article a WHERE a.status = :status")
        long countByStatus(@Param("status") ArticleStatus status);

        @Query("SELECT a FROM Article a WHERE a.status = :status ORDER BY a.updatedAt DESC")
        Page<Article> findLatestArticles(@Param("status") ArticleStatus status, Pageable pageable);

        @Query("SELECT a FROM Article a " +
                        "WHERE a.status = :status " +
                        "AND EXISTS (SELECT 1 FROM Comment c WHERE c.article = a AND c.createdAt >= :startDate) " +
                        "ORDER BY (SELECT COUNT(c2) FROM Comment c2 WHERE c2.article = a AND c2.createdAt >= :startDate) DESC")
        Page<Article> findHotArticles(
                        @Param("status") ArticleStatus status,
                        @Param("startDate") LocalDateTime startDate,
                        Pageable pageable);
}