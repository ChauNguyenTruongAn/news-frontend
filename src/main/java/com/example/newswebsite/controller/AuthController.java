package com.example.newswebsite.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.example.newswebsite.domain.RefreshToken;
import com.example.newswebsite.domain.User;
import com.example.newswebsite.service.JwtService;
import com.example.newswebsite.service.RefreshTokenService;
import com.example.newswebsite.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "API Xác thực", description = "API xử lý xác thực người dùng và quản lý token")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${frontend.redirect-url}")
    private String frontendRedirectUrl;

    @GetMapping("/google-callback")
    @Operation(summary = "Xử lý callback từ Google OAuth", description = "Xử lý mã code từ Google để đăng nhập và trả về access token cùng refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Chuyển hướng về frontend với access_token và role"),
            @ApiResponse(responseCode = "400", description = "Mã code không hợp lệ hoặc lỗi xác thực"),
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public void googleCallback(
            @Parameter(description = "Mã code từ Google OAuth") @RequestParam("code") String code,
            HttpServletResponse response) throws IOException {
        String tokenUrl = "https://oauth2.googleapis.com/token";
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("redirect_uri", redirectUri);
        params.put("grant_type", "authorization_code");

        Map<String, Object> tokenResponse = restTemplate.postForObject(tokenUrl, params, Map.class);
        if (tokenResponse == null || tokenResponse.containsKey("error")) {
            response.sendRedirect(frontendRedirectUrl + "/?error=login_failed");
            return;
        }

        String idToken = (String) tokenResponse.get("id_token");
        OidcUser oidcUser;
        try {
            oidcUser = jwtService.parseGoogleIdToken(idToken);
        } catch (IllegalArgumentException e) {
            response.sendRedirect(frontendRedirectUrl + "/?error=login_failed");
            return;
        }

        User user = userService.saveOrUpdateUser(oidcUser);
        String accessToken = jwtService.generateToken(user.getGoogleId(), user.getRole().getRoleName());
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(24 * 60 * 60);
        response.addCookie(refreshCookie);

        String redirectUrl = String.format(
                "%s/callback?access_token=%s&role=%s&name=%s&email=%s&picture=%s&googleId=%s",
                frontendRedirectUrl,
                URLEncoder.encode(accessToken, StandardCharsets.UTF_8),
                URLEncoder.encode(user.getRole().getRoleName(), StandardCharsets.UTF_8),
                URLEncoder.encode(user.getName() != null ? user.getName() : "", StandardCharsets.UTF_8),
                URLEncoder.encode(user.getEmail() != null ? user.getEmail() : "", StandardCharsets.UTF_8),
                URLEncoder.encode(user.getAvatarUrl() != null ? user.getAvatarUrl() : "", StandardCharsets.UTF_8),
                URLEncoder.encode(user.getGoogleId(), StandardCharsets.UTF_8));

        response.sendRedirect(redirectUrl);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token", description = "Sử dụng refresh token để tạo mới access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access token mới được tạo thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token không hợp lệ hoặc hết hạn"),
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public ResponseEntity<Map<String, String>> refreshToken(
            @Parameter(description = "Refresh token từ cookie") @CookieValue(name = "refresh_token", required = false) String refreshToken) {
        if (refreshToken == null || !refreshTokenService.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token không hợp lệ hoặc hết hạn"));
        }

        Optional<RefreshToken> tokenOpt = refreshTokenService.findByToken(refreshToken);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Không tìm thấy refresh token"));
        }

        User user = tokenOpt.get().getUser();
        String newAccessToken = jwtService.generateToken(user.getGoogleId(), user.getRole().getRoleName());

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("access_token", newAccessToken);
        responseBody.put("googleId", user.getGoogleId());
        responseBody.put("role", user.getRole().getRoleName());
        responseBody.put("name", user.getName() != null ? user.getName() : "");
        responseBody.put("email", user.getEmail() != null ? user.getEmail() : "");
        responseBody.put("picture", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất", description = "Xóa refresh token và đăng xuất người dùng")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng xuất thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        Cookie refreshCookie = new Cookie("refresh_token", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addHeader("Set-Cookie", "refresh_token=; HttpOnly; SameSite=Strict; Path=/; Max-Age=0");
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    @GetMapping("/direct-callback")
    @Operation(summary = "Xử lý callback trực tiếp", description = "Xử lý callback với các tham số đã có")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thông tin người dùng và token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Thông tin không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public ResponseEntity<Map<String, String>> directCallback(
            @RequestParam("access_token") String accessToken,
            @RequestParam("role") String role,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("picture") String pictureUrl,
            @RequestParam(value = "googleId", required = false) String googleId) {

        Map<String, String> response = new HashMap<>();
        response.put("access_token", accessToken);
        response.put("role", role);
        response.put("name", name);
        response.put("email", email);
        response.put("picture", pictureUrl);
        response.put("googleId", googleId != null ? googleId : "");

        return ResponseEntity.ok(response);
    }
}