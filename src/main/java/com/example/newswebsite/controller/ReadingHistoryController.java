package com.example.newswebsite.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.newswebsite.domain.ReadingHistory;
import com.example.newswebsite.domain.response.ReadingHistorySummaryDTO;
import com.example.newswebsite.service.ReadingHistoryService;

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
@RequestMapping("/api/reading-history")
@RequiredArgsConstructor
@Tag(name = "API lịch sử xem", description = "Quản lý lịch sử xem")
public class ReadingHistoryController {

    private final ReadingHistoryService readingHistoryService;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Lấy lịch sử xem của user", description = "Lấy toàn bộ danh sách lịch sử xem của người dùng", security = {
            @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy lịch sử thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Access token không đủ quyền hạn")
    })
    public ResponseEntity<List<ReadingHistory>> getReadingHistory() {
        List<ReadingHistory> histories = readingHistoryService.getUserReadingHistory();
        return ResponseEntity.ok(histories);
    }

    @GetMapping("/user/{googleId}")
    @PreAuthorize("hasRole('USER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Lấy lịch sử đọc và lượt xem bài báo đó", description = "Lấy về lịch sử xem và dùng group by để trả ra lượt xem của bài báo đó", security = {
            @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<List<ReadingHistorySummaryDTO>> getUserReadingHistorySummary(@PathVariable String googleId) {
        List<ReadingHistorySummaryDTO> histories = readingHistoryService.getUserReadingHistoryByGoogleId(googleId);
        return ResponseEntity.ok(histories);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Xóa lịch sử xem", description = "Xóa lịch sử xem", security = {
            @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<Void> deleteReadingHistory(@PathVariable Integer id) {
        boolean deleted = readingHistoryService.deleteReadingHistory(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}