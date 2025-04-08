package com.example.newswebsite.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.newswebsite.domain.Article;
import com.example.newswebsite.domain.Comment;
import com.example.newswebsite.domain.User;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticleArticleIdAndParentCommentIsNull(Long articleId);

    Page<Comment> findByArticle(Article article, Pageable pageable);

    Page<Comment> findByParentComment(Comment parentComment, Pageable pageable);

    Page<Comment> findByAuthor(User author, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.article.articleId = :articleId")
    long countByArticleArticleId(@Param("articleId") Integer articleId);
}