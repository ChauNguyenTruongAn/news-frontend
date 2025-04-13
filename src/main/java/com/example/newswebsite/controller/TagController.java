package com.example.newswebsite.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.newswebsite.domain.Tag;
import com.example.newswebsite.service.TagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Slf4j
@io.swagger.v3.oas.annotations.tags.Tag(name = "API Quản lý Tag", description = "API quản lý tag")
public class TagController {
    private final TagService tagService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tag", description = "Lấy tất cả các tag")
    public ResponseEntity<List<Tag>> getAllTags() {
        log.info("Getting all tags");
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @PostMapping
    @Operation(summary = "Tạo tag mới", description = "Tạo một tag mới")
    public ResponseEntity<Tag> createTag(
            @Parameter(description = "Tên tag") @RequestBody String name) {
        log.info("Creating new tag: {}", name);
        return ResponseEntity.ok(tagService.createTag(name));
    }

    @DeleteMapping("/{tagId}")
    @Operation(summary = "Xóa tag", description = "Xóa một tag theo ID")
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "ID của tag cần xóa") @PathVariable Integer tagId) {
        log.info("Deleting tag with id: {}", tagId);
        tagService.deleteTag(tagId);
        return ResponseEntity.ok().build();
    }
}