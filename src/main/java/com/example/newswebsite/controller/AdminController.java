package com.example.newswebsite.controller;

import com.example.newswebsite.domain.Article;
import com.example.newswebsite.domain.Category;
import com.example.newswebsite.domain.User;
import com.example.newswebsite.domain.request.ArticleUpdateRequest;
import com.example.newswebsite.domain.request.CategoryRequest;
import com.example.newswebsite.domain.request.UserRoleUpdateRequest;
import com.example.newswebsite.domain.response.ArticleDTO;
import com.example.newswebsite.service.ArticleService;
import com.example.newswebsite.service.CategoryService;
import com.example.newswebsite.service.JwtService;
import com.example.newswebsite.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "API Quản trị", description = "API dành cho các thao tác quản trị bài viết và người dùng")
@SecurityRequirement(name = "bearerAuth") // Yêu cầu JWT cho tất cả endpoint
public class AdminController {

        private final ArticleService articleService;
        private final CategoryService categoryService;
        private final UserService userService;
        private final JwtService jwtService;

        @PutMapping("/articles/{id}/publish")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Xuất bản bài viết", description = "Xuất bản một bài viết với ID được chỉ định. Chỉ người dùng có vai trò ADMIN mới truy cập được.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bài viết được xuất bản thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Article.class))),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Người dùng không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Article> publishArticle(
                        @Parameter(description = "ID của bài viết cần xuất bản", required = true) @PathVariable Integer id,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'", required = true) @RequestHeader("Authorization") String token) {
                String googleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
                Article published = articleService.publishArticle(id, googleId);
                return ResponseEntity.ok(published);
        }

        @PutMapping("/articles/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Cập nhật bài viết", description = "Cập nhật thông tin chi tiết của bài viết với ID được chỉ định. Chỉ người dùng có vai trò ADMIN mới truy cập được.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bài viết được cập nhật thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Article.class))),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Người dùng không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Article> updateArticle(
                        @Parameter(description = "ID của bài viết cần cập nhật", required = true) @PathVariable Integer id,
                        @Parameter(description = "Dữ liệu yêu cầu chứa thông tin cập nhật bài viết", required = true) @RequestBody ArticleUpdateRequest updateRequest,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'", required = true) @RequestHeader("Authorization") String token) {
                String googleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
                Article updated = articleService.updateArticle(id, updateRequest, googleId);
                return ResponseEntity.ok(updated);
        }

        @DeleteMapping("/articles/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Xóa bài viết", description = "Xóa một bài viết với ID được chỉ định. Chỉ người dùng có vai trò ADMIN mới truy cập được.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Bài viết được xóa thành công"),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Người dùng không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Void> deleteArticle(
                        @Parameter(description = "ID của bài viết cần xóa", required = true) @PathVariable Integer id,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'", required = true) @RequestHeader("Authorization") String token) {
                String googleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
                articleService.deleteArticle(id, googleId);
                return ResponseEntity.noContent().build();
        }

        @PostMapping("/articles/{id}/tags")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Thêm thẻ vào bài viết", description = "Thêm danh sách các thẻ vào bài viết với ID được chỉ định. Chỉ người dùng có vai trò ADMIN mới truy cập được.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thẻ được thêm thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Article.class))),
                        @ApiResponse(responseCode = "400", description = "Tên thẻ không hợp lệ"),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Người dùng không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Article> addTags(
                        @Parameter(description = "ID của bài viết để thêm thẻ", required = true) @PathVariable Integer id,
                        @Parameter(description = "Danh sách tên thẻ cần thêm", required = true) @RequestBody List<String> tagNames,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'", required = true) @RequestHeader("Authorization") String token) {
                String googleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
                Article updated = articleService.addTagsToArticle(id, tagNames, googleId);
                return ResponseEntity.ok(updated);
        }

        @PutMapping("/users/{googleId}/approve-editor")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Phê duyệt vai trò biên tập viên cho người dùng", description = "Phê duyệt vai trò biên tập viên cho người dùng với Google ID được chỉ định. Chỉ người dùng có vai trò ADMIN mới truy cập được.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Vai trò biên tập viên được phê duyệt thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Người dùng không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<User> approveEditorRole(
                        @Parameter(description = "Google ID của người dùng để phê duyệt vai trò biên tập viên", required = true) @PathVariable String googleId,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'", required = true) @RequestHeader("Authorization") String token) {
                String adminGoogleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
                User user = userService.approveEditorRole(googleId, adminGoogleId);
                return ResponseEntity.ok(user);
        }

        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Thêm danh mục mới", description = "Tạo một danh mục mới, chỉ dành cho ADMIN")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thêm danh mục thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Category.class))),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Category> addCategory(
                        @Parameter(description = "Thông tin danh mục mới (tên, mô tả, danh mục cha nếu có)") @RequestBody Category category) {
                Category savedCategory = categoryService.saveCategory(category);
                return ResponseEntity.ok(savedCategory);
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Chỉnh sửa danh mục", description = "Cập nhật thông tin danh mục với ID được chỉ định, chỉ dành cho ADMIN")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Cập nhật danh mục thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Category.class))),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Category> updateCategory(
                        @Parameter(description = "ID của danh mục cần chỉnh sửa") @PathVariable Integer id,
                        @Parameter(description = "Thông tin danh mục cập nhật (tên, mô tả, danh mục cha nếu có)") @RequestBody Category category)
                        throws Exception {
                Category existingCategory = categoryService.findById(id);

                existingCategory.setName(category.getName());
                existingCategory.setDescription(category.getDescription());
                existingCategory.setParent(category.getParent());

                Category updatedCategory = categoryService.saveCategory(existingCategory);
                return ResponseEntity.ok(updatedCategory);
        }

        @GetMapping("/top-level")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Lấy danh sách danh mục cấp cao", description = "Trả về danh sách các danh mục không có danh mục cha, chỉ dành cho ADMIN")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Category.class))),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<List<Category>> getTopLevelCategories() {
                List<Category> topLevelCategories = categoryService.findByParentIsNull();
                return ResponseEntity.ok(topLevelCategories);
        }

        @GetMapping("/articles")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Lấy tất cả bài viết", description = "Trả về danh sách tất cả bài viết không phân biệt trạng thái, chỉ dành cho ADMIN")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Page<ArticleDTO>> getArticles(Pageable pageable) {
                Page<ArticleDTO> articles = articleService.findAllArticlesAsDTO(pageable);
                return ResponseEntity.ok(articles);
        }

        @DeleteMapping("/category/{categoryId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Xóa danh mục", description = "Xóa danh mục theo ID, chỉ dành cho ADMIN")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Danh mục được xóa thành công"),
                        @ApiResponse(responseCode = "400", description = "Danh mục không thể xóa vì còn bài viết liên quan"),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<Void> deleteCategory(
                        @Parameter(description = "ID của danh mục cần xóa") @PathVariable Integer categoryId) {
                try {
                        categoryService.deleteCategory(categoryId);
                        return ResponseEntity.noContent().build();
                } catch (Exception e) {
                        return ResponseEntity.badRequest().build();
                }
        }

        @GetMapping("/users")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Lấy danh sách người dùng", description = "Trả về danh sách tất cả người dùng, chỉ dành cho ADMIN")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<List<User>> getAllUsers() {
                List<User> users = userService.findAllUsers();
                return ResponseEntity.ok(users);
        }

        @PutMapping("/users/{googleId}/role")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Chỉnh sửa vai trò người dùng", description = "Cập nhật vai trò của người dùng (user/editor/admin), chỉ dành cho ADMIN")
        @SecurityRequirement(name = "bearerAuth")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Cập nhật vai trò thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
                        @ApiResponse(responseCode = "400", description = "Vai trò không hợp lệ hoặc không tìm thấy người dùng"),
                        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
                        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN"),
                        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
        })
        public ResponseEntity<User> updateUserRole(
                        @Parameter(description = "Google ID của người dùng cần thay đổi quyền") @PathVariable String googleId,
                        @Parameter(description = "Thông tin vai trò mới") @RequestBody UserRoleUpdateRequest request,
                        @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'") @RequestHeader("Authorization") String token) {

                try {
                        String adminGoogleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
                        User updatedUser = userService.updateUserRole(googleId, request.getRoleName(), adminGoogleId);
                        return ResponseEntity.ok(updatedUser);
                } catch (Exception e) {
                        return ResponseEntity.badRequest().build();
                }
        }

}