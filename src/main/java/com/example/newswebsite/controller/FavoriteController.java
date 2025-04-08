package com.example.newswebsite.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.newswebsite.domain.Favorite;
import com.example.newswebsite.domain.request.FavoriteRequest;
import com.example.newswebsite.domain.response.FavoriteSummaryDTO;
import com.example.newswebsite.service.FavoriteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "API yêu thích", description = "API quản lý yêu thích")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Thêm yêu thích cho bài báo", description = "Thêm bài báo yêu thích cho người dùng hiện tại", security = {
            @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Yêu thích thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không đủ quyền hạn")
    })
    public ResponseEntity<Favorite> addFavorite(@RequestBody FavoriteRequest request) throws Exception {
        Favorite favorite = favoriteService.saveFavorite(request);
        return ResponseEntity.ok(favorite);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách yêu thích của user", description = "Trả về toàn bộ danh sách yêu thích bởi user", security = {
            @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<List<Favorite>> getFavorites() {
        List<Favorite> favorites = favoriteService.getUserFavorites();
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/user/{googleId}")
    @PreAuthorize("hasRole('USER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách theo google id", description = "Trả về danh sách yêu thích theo google id", security = {
            @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<List<FavoriteSummaryDTO>> getUserFavorites(@PathVariable String googleId) {
        List<FavoriteSummaryDTO> favorites = favoriteService.getUserFavoritesByGoogleId(googleId);
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/article/{articleId}/user/{googleId}")
    @PreAuthorize("hasRole('USER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Kiểm tra yêu thích", description = "Trả về true nếu user đã yêu thích bài báo", security = {
            @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<Boolean> isArticleFavorited(
            @PathVariable Integer articleId,
            @PathVariable String googleId) {
        boolean isFavorited = favoriteService.isArticleFavoritedByUser(articleId, googleId);
        return ResponseEntity.ok(isFavorited);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Hủy bỏ yêu thích", description = "Hủy bỏ yêu thích bài viết, sử dụng google id và id bài báo hiện tại để xác định", security = {
            @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<Void> deleteFavorite(@PathVariable Integer id) {
        boolean deleted = favoriteService.deleteFavorite(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}