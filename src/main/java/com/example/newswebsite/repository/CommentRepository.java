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
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    @Query("SELECT c FROM Comment c WHERE c.article.articleId = :articleId AND c.parent IS NULL")
    List<Comment> findByArticleArticleIdAndParentIsNull(@Param("articleId") Integer articleId);

    Page<Comment> findByArticle(Article article, Pageable pageable);

    Page<Comment> findByAuthor(User author, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.parent.commentId = :parentId")
    List<Comment> findByParentId(@Param("parentId") Integer parentId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.article.articleId = :articleId")
    long countByArticleArticleId(@Param("articleId") Integer articleId);
}