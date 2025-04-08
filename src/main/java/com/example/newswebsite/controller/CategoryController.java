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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.newswebsite.domain.Category;
import com.example.newswebsite.domain.request.CategoryRequest;
import com.example.newswebsite.domain.response.CategoryTreeDTO;
import com.example.newswebsite.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "API Danh mục", description = "API quản lý cấu trúc danh mục")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/parent")
    @Operation(summary = "Lấy danh sách danh mục cha", description = "Trả về danh sách các danh mục không có danh mục cha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Category.class)))),
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public ResponseEntity<List<Category>> getParentCategories() {
        log.info("Getting parent categories");
        List<Category> categories = categoryService.findByParentIsNull();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/children/{parentId}")
    @Operation(summary = "Lấy danh sách danh mục con", description = "Trả về danh sách các danh mục con của một danh mục cha cụ thể")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Category.class)))),
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public ResponseEntity<List<Category>> getChildCategories(
            @Parameter(description = "ID của danh mục cha") @PathVariable Integer parentId) {
        log.info("Getting child categories for parent ID: {}", parentId);
        List<Category> categories = categoryService.findChildCategories(parentId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/tree")
    @Operation(summary = "Lấy cấu trúc cây danh mục", description = "Trả về cấu trúc cây đầy đủ của tất cả danh mục (2 cấp: cha và con)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CategoryTreeDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public ResponseEntity<List<CategoryTreeDTO>> getCategoryTree() {
        log.info("Getting category tree");
        List<CategoryTreeDTO> categoryTree = categoryService.getCategoryTree();
        return ResponseEntity.ok(categoryTree);
    }

    @PostMapping("/parent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm danh mục cha", description = "Tạo một danh mục cha mới (không có parent), chỉ dành cho ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Danh mục được tạo thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Category.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
            @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN"),
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public ResponseEntity<Category> createParentCategory(
            @RequestBody CategoryRequest request) {
        log.info("Creating parent category with name: {}", request.getName());
        Category category = categoryService.createParentCategory(request.getName(), request.getDescription());
        return ResponseEntity.ok(category);
    }

    @PostMapping("/child")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm danh mục con", description = "Tạo một danh mục con với parent được chỉ định, chỉ dành cho ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Danh mục được tạo thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Category.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ hoặc không tìm thấy danh mục cha"),
            @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
            @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN"),
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public ResponseEntity<Category> createChildCategory(
            @RequestBody CategoryRequest request) {
        log.info("Creating child category with name: {} and parentId: {}", request.getName(), request.getParentId());
        if (request.getParentId() == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Category category = categoryService.createChildCategory(request.getName(), request.getDescription(),
                    request.getParentId());
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            log.error("Error creating child category: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật danh mục", description = "Cập nhật thông tin danh mục (tên, mô tả), chỉ dành cho ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Danh mục được cập nhật thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Category.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
            @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục"),
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public ResponseEntity<Category> updateCategory(
            @Parameter(description = "ID của danh mục cần cập nhật") @PathVariable Integer categoryId,
            @Parameter(description = "Thông tin cập nhật danh mục") @RequestBody CategoryRequest request) {
        log.info("Updating category with ID: {}", categoryId);
        try {
            Category category = categoryService.updateCategory(categoryId, request);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            log.error("Error updating category: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa danh mục", description = "Xóa danh mục theo ID, chỉ dành cho ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Danh mục được xóa thành công"),
            @ApiResponse(responseCode = "400", description = "Danh mục không thể xóa vì còn bài viết liên quan hoặc có danh mục con"),
            @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
            @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò ADMIN"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục"),
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID của danh mục cần xóa") @PathVariable Integer categoryId) {
        log.info("Deleting category with ID: {}", categoryId);
        try {
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting category: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}