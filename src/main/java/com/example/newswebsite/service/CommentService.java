package com.example.newswebsite.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.newswebsite.domain.Article;
import com.example.newswebsite.domain.Comment;
import com.example.newswebsite.domain.User;
import com.example.newswebsite.repository.ArticleRepository;
import com.example.newswebsite.repository.CommentRepository;
import com.example.newswebsite.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userService;

    public Comment addComment(Integer articleId, String content, Integer parentCommentId, String googleId) {
        User author = userService.findByGoogleId(googleId);
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setArticle(article);
        comment.setCreatedAt(LocalDateTime.now());
        if (comment.getReplies() == null) {
            comment.setReplies(new ArrayList<>());
        }
        if (parentCommentId != null) {
            Comment parent = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParent(parent);
            if (parent.getReplies() == null) {
                parent.setReplies(new ArrayList<>());
            }
            parent.getReplies().add(comment);
        }
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByArticle(Integer articleId) {
        return commentRepository.findByArticleArticleIdAndParentIsNull(articleId);
    }

    public void deleteComment(Integer commentId, String googleId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        User user = userService.findByGoogleId(googleId);

        if (!comment.getAuthor().getGoogleId().equals(googleId) && !user.getRole().getRoleName().equals("admin")) {
            throw new RuntimeException("Unauthorized: Only the comment author or admin can delete this comment");
        }

        commentRepository.delete(comment);
    }

    public Comment editComment(Integer commentId, String newContent, String googleId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        User user = userService.findByGoogleId(googleId);

        if (!comment.getAuthor().getGoogleId().equals(googleId) && !user.getRole().getRoleName().equals("admin")) {
            throw new RuntimeException("Unauthorized: Only the comment author or admin can edit this comment");
        }

        comment.setContent(newContent);
        return commentRepository.save(comment);
    }
}