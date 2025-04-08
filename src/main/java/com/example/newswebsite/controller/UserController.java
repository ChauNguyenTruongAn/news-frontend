package com.example.newswebsite.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.newswebsite.domain.User;
import com.example.newswebsite.service.JwtService;
import com.example.newswebsite.service.UserService;

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
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "API Người dùng", description = "API quản lý thông tin và vai trò người dùng")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/request-editor")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Yêu cầu vai trò biên tập viên", description = "Gửi yêu cầu nâng cấp vai trò thành EDITOR, chỉ dành cho USER")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Yêu cầu vai trò biên tập viên thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "401", description = "Không được phép - Token JWT không hợp lệ hoặc thiếu"),
        @ApiResponse(responseCode = "403", description = "Bị từ chối - Không có vai trò USER"),
        @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public ResponseEntity<User> requestEditorRole(
            @Parameter(description = "Token JWT theo định dạng 'Bearer <token>'") @RequestHeader("Authorization") String token) {
        String googleId = jwtService.getGoogleIdFromToken(token.replace("Bearer ", ""));
        User user = userService.requestEditorRole(googleId);
        return ResponseEntity.ok(user);
    }
}