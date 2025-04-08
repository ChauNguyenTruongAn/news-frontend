package com.example.newswebsite.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import com.example.newswebsite.domain.Article;
import com.example.newswebsite.domain.ReadingHistory;
import com.example.newswebsite.domain.request.ArticleRequest;
import com.example.newswebsite.domain.request.ArticleUpdateRequest;
import com.example.newswebsite.domain.response.ArticleDTO;
import com.example.newswebsite.domain.response.CategoryTreeDTO;
import com.example.newswebsite.service.ArticleService;
import com.example.newswebsite.service.JwtService;
import com.example.newswebsite.specification.ArticleSpecification;
import com.example.newswebsite.util.ArticleStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "API Bài viết", description = "API quản lý bài viết và lịch sử đọc")
@Slf4j
public class ArticleController {
        private final ArticleService articleService;
        private final JwtService jwtService;

        @GetMapping("/home")
        @Operation(summary = "Lấy danh sách bài viết cho trang chủ", description = "Trả về danh sách bài viết phân trang cho trang chủ")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleDTO.class))),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Page<ArticleDTO>> getArticlesForHome(Pageable pageable) {
                Page<ArticleDTO> articles = articleService.getArticlesForHome(pageable);
                return ResponseEntity.ok(articles);
        }

        @GetMapping("/category/{categoryId}")
        @Operation(summary = "Lấy bài viết theo danh mục", description = "Trả về danh sách bài viết phân trang theo danh mục")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Page<ArticleDTO>> getArticlesByCategory(
                        @Parameter(description = "ID của danh mục") @PathVariable Integer categoryId,
                        Pageable pageable) {
                Page<ArticleDTO> articles = articleService.getArticlesByCategory(categoryId, pageable);
                return ResponseEntity.ok(articles);
        }

        @GetMapping("/categories")
        @Operation(summary = "Lấy cây danh mục", description = "Trả về danh sách danh mục dạng cây")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryTreeDTO.class))),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<List<CategoryTreeDTO>> getCategoryTree() {
                List<CategoryTreeDTO> categories = articleService.getCategoryTree();
                return ResponseEntity.ok(categories);
        }

        @GetMapping
        @Operation(summary = "Tìm kiếm bài viết đã xuất bản", description = "Tìm kiếm bài viết đã xuất bản theo tiêu đề và danh mục, trả về kết quả phân trang")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Article.class))),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Page<Article>> getArticles(
                        @Parameter(description = "Tiêu đề bài viết (tùy chọn)") @RequestParam(required = false) String title,
                        @Parameter(description = "ID danh mục (tùy chọn)") @RequestParam(required = false) Integer categoryId,
                        Pageable pageable) {
                return ResponseEntity
                                .ok(articleService.findArticles(ArticleSpecification.searchArticles(title,
                                                categoryId), pageable));
        }

        @PostMapping
        @Operation(summary = "Tạo bài viết mới", description = "Tạo một bài viết mới với thông tin cung cấp")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bài viết được tạo thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Article.class))),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Article> createArticle(
                        @Parameter(description = "Thông tin bài viết mới") @RequestBody ArticleRequest articleRequest,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'") @RequestHeader("Authorization") String token)
                        throws Exception {
                String googleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
                Article article = new Article();
                article.setTitle(articleRequest.getTitle());
                article.setSummary(articleRequest.getSummary());
                article.setContent(articleRequest.getContent());
                article.setThumbnailUrl(articleRequest.getThumbnailUrl());

                Article created = articleService.createArticle(
                                article,
                                articleRequest.getCategoryId(),
                                articleRequest.getTagNames(),
                                googleId);
                return ResponseEntity.ok(created);
        }

        @PutMapping("/{id}/publish")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Xuất bản bài viết", description = "Xuất bản bài viết với ID được chỉ định, chỉ dành cho ADMIN")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Xuất bản thành công"),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<String> publishArticle(
                        @Parameter(description = "ID của bài viết") @PathVariable Integer id,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'") @RequestHeader("Authorization") String token) {
                String googleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
                Article published = articleService.publishArticle(id, googleId);
                return ResponseEntity.ok("Xuất bản bài viết thành công: " + published.getTitle());
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
        @Operation(summary = "Cập nhật bài viết", description = "Cập nhật bài viết với ID được chỉ định, chỉ dành cho ADMIN")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Cập nhật thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Article.class))),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Article> updateArticle(
                        @Parameter(description = "ID của bài viết") @PathVariable Integer id,
                        @Parameter(description = "Thông tin cập nhật bài viết") @RequestBody ArticleUpdateRequest article,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'") @RequestHeader("Authorization") String token) {
                String googleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
                Article updated = articleService.updateArticle(id, article, googleId);
                return ResponseEntity.ok(updated);
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Xóa bài viết", description = "Xóa bài viết với ID được chỉ định, chỉ dành cho ADMIN")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Xóa thành công"),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Void> deleteArticle(
                        @Parameter(description = "ID của bài viết") @PathVariable Integer id,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'") @RequestHeader("Authorization") String token) {
                String googleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
                articleService.deleteArticle(id, googleId);
                return ResponseEntity.noContent().build();
        }

        @PostMapping("/{id}/tags")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Thêm thẻ vào bài viết", description = "Thêm danh sách thẻ vào bài viết với ID được chỉ định, chỉ dành cho ADMIN")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thêm thẻ thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Article.class))),
                        @ApiResponse(responseCode = "400", description = "Tên thẻ không hợp lệ"),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Article> addTags(
                        @Parameter(description = "ID của bài viết") @PathVariable Integer id,
                        @Parameter(description = "Danh sách tên thẻ") @RequestBody List<String> tagNames,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'") @RequestHeader("Authorization") String token) {
                String googleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
                Article updated = articleService.addTagsToArticle(id, tagNames, googleId);
                return ResponseEntity.ok(updated);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Lấy chi tiết bài viết", description = "Trả về thông tin chi tiết của bài viết theo ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<ArticleDTO> getArticleById(
                        @Parameter(description = "ID của bài viết") @PathVariable Integer id,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>' (tùy chọn)") @RequestHeader(value = "Authorization", required = false) String token) {
                String googleId = token != null && token.startsWith("Bearer ")
                                ? jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""))
                                : null;
                ArticleDTO article = articleService.getArticleById(id, googleId);
                return ResponseEntity.ok(article);
        }

        @GetMapping("/history")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        @Operation(summary = "Lấy lịch sử đọc", description = "Trả về danh sách lịch sử đọc của người dùng")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReadingHistory.class))),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò USER hoặc ADMIN"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<List<ReadingHistory>> getReadingHistory(
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'") @RequestHeader("Authorization") String token) {
                String googleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
                List<ReadingHistory> history = articleService.getReadingHistory(googleId);
                return ResponseEntity.ok(history);
        }

        @GetMapping("/author/{googleId}")
        @Operation(summary = "Lấy bài viết theo tác giả", description = "Trả về danh sách bài viết của tác giả theo Google ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy tác giả"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Page<ArticleDTO>> getArticlesByAuthorGoogleId(
                        @Parameter(description = "Google ID của tác giả") @PathVariable String googleId,
                        Pageable pageable) {
                Page<ArticleDTO> articles = articleService.getArticlesByAuthorGoogleId(googleId, pageable);
                return ResponseEntity.ok(articles);
        }

        @GetMapping("/detail/{id}")
        @Operation(summary = "Lấy chi tiết bài viết đã xuất bản với thông tin tác giả", description = "Trả về thông tin chi tiết của bài viết đã xuất bản kèm thông tin tác giả")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết hoặc bài viết chưa xuất bản"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<ArticleDTO> getArticleWithAuthorDetails(
                        @Parameter(description = "ID của bài viết") @PathVariable Integer id,
                        @Parameter(description = "GoogleID của tác giả (tùy chọn)") @RequestParam(required = false) String authorGoogleId) {

                ArticleDTO article = articleService.getPublishedArticleById(id, authorGoogleId);
                if (article == null) {
                        return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(article);
        }

        @GetMapping("/admin/search")
        @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
        @Operation(summary = "Tìm kiếm bài viết theo trạng thái (Admin)", description = "Tìm kiếm bài viết theo tiêu đề, danh mục, và trạng thái, chỉ dành cho ADMIN và EDITOR")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Article.class))),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN hoặc EDITOR"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Page<Article>> searchArticlesByStatus(
                        @Parameter(description = "Tiêu đề bài viết (tùy chọn)") @RequestParam(required = false) String title,
                        @Parameter(description = "ID danh mục (tùy chọn)") @RequestParam(required = false) Integer categoryId,
                        @Parameter(description = "Trạng thái bài viết (tùy chọn, mặc định là PUBLISHED)") @RequestParam(required = false, defaultValue = "PUBLISHED") String status,
                        Pageable pageable,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'") @RequestHeader("Authorization") String token) {

                // Trích xuất googleId từ token
                String googleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));

                // Chuyển đổi trạng thái từ chuỗi sang enum
                ArticleStatus articleStatus;
                try {
                        articleStatus = ArticleStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                        articleStatus = ArticleStatus.PUBLISHED; // Mặc định là PUBLISHED nếu không hợp lệ
                }

                // Tìm kiếm bài viết với trạng thái chỉ định
                return ResponseEntity.ok(articleService.findArticles(
                                ArticleSpecification.searchArticlesWithStatus(title, categoryId, articleStatus),
                                pageable));
        }

        @GetMapping("/latest")
        public ResponseEntity<Page<Article>> getLatestArticles(Pageable pageable) {
                log.info("Getting latest articles");
                Page<Article> articles = articleService.getLatestArticles(pageable);
                return ResponseEntity.ok(articles);
        }

        @GetMapping("/hot")
        public ResponseEntity<Page<Article>> getHotArticles(Pageable pageable) {
                log.info("Getting hot articles");
                Page<Article> articles = articleService.getHotArticles(pageable);
                return ResponseEntity.ok(articles);
        }
}