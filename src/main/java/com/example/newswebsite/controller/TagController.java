package com.example.newswebsite.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.newswebsite.domain.Tag;
import com.example.newswebsite.service.TagService;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "API Thẻ", description = "API quản lý thẻ (tag) cho bài viết")
public class TagController {

    private final TagService tagService;

    @PostMapping
    @PreAuthorize("hasRole('editor') or hasRole('admin')")
    @Operation(summary = "Tạo thẻ mới", description = "Tạo một thẻ mới, yêu cầu vai trò EDITOR hoặc ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo thẻ thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Tag.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
            @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò EDITOR hoặc ADMIN"),
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public ResponseEntity<Tag> createTag(
            @Parameter(description = "Thông tin thẻ mới") @RequestBody Tag tag) {
        Tag savedTag = tagService.saveTag(tag);
        return ResponseEntity.ok(savedTag);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách thẻ", description = "Trả về danh sách tất cả các thẻ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thẻ thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Tag.class))),
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public ResponseEntity<List<Tag>> getTags() {
        List<Tag> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }
}