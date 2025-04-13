package com.example.newswebsite.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.newswebsite.domain.Comment;
import com.example.newswebsite.domain.request.CommentRequest;
import com.example.newswebsite.service.CommentService;
import com.example.newswebsite.service.JwtService;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "API Bình luận", description = "API quản lý bình luận cho bài viết")
public class CommentController {
        private final CommentService commentService;
        private final JwtService jwtService;

        @PostMapping
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        @Operation(summary = "Thêm bình luận", description = "Thêm một bình luận mới cho bài viết, yêu cầu vai trò USER hoặc ADMIN")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thêm bình luận thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Comment.class))),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò USER hoặc ADMIN"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Comment> addComment(
                        @RequestBody CommentRequest request,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'") @RequestHeader("Authorization") String token) {
                String googleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
                Comment comment = commentService.addComment(request.getArticleId(), request.getContent(),
                                request.getParentCommentId(), googleId);
                return ResponseEntity.ok(comment);
        }

        @GetMapping("/article/{articleId}")
        @Operation(summary = "Lấy danh sách bình luận theo bài viết", description = "Trả về danh sách bình luận của bài viết theo ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Comment.class))),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<List<Comment>> getCommentsByArticle(
                        @Parameter(description = "ID của bài viết") @PathVariable Integer articleId) {
                List<Comment> comments = commentService.getCommentsByArticle(articleId);
                return ResponseEntity.ok(comments);
        }

        @DeleteMapping("/{commentId}")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        @Operation(summary = "Xóa bình luận", description = "Xóa bình luận với ID được chỉ định, yêu cầu vai trò USER hoặc ADMIN")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Xóa bình luận thành công"),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có quyền xóa bình luận"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bình luận"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Void> deleteComment(
                        @Parameter(description = "ID của bình luận cần xóa") @PathVariable Integer commentId,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'") @RequestHeader("Authorization") String token) {
                String googleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
                commentService.deleteComment(commentId, googleId);
                return ResponseEntity.noContent().build();
        }

        @PutMapping("/{commentId}")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        @Operation(summary = "Sửa bình luận", description = "Cập nhật nội dung bình luận với ID được chỉ định, yêu cầu vai trò USER hoặc ADMIN")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Cập nhật bình luận thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Comment.class))),
                        @ApiResponse(responseCode = "400", description = "Nội dung không hợp lệ"),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có quyền sửa bình luận"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bình luận"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Comment> editComment(
                        @Parameter(description = "ID của bình luận cần sửa") @PathVariable Integer commentId,
                        @Parameter(description = "Nội dung bình luận mới") @RequestParam String content,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'") @RequestHeader("Authorization") String token) {
                String googleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
                Comment updatedComment = commentService.editComment(commentId, content, googleId);
                return ResponseEntity.ok(updatedComment);
        }
}