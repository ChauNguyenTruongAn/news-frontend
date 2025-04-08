package com.example.newswebsite.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/callback")
@Tag(name = "API Callback", description = "API xử lý callback authentication từ dịch vụ bên ngoài")
public class CallbackController {

    @GetMapping
    @Operation(summary = "Xử lý callback từ dịch vụ xác thực", description = "Xử lý thông tin người dùng từ callback authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thông tin người dùng và token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Thông tin không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public ResponseEntity<Map<String, String>> handleCallback(
            @RequestParam(value = "access_token", required = true) String accessToken,
            @RequestParam(value = "role", required = true) String role,
            @RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "email", required = true) String email,
            @RequestParam(value = "picture", required = true) String pictureUrl,
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