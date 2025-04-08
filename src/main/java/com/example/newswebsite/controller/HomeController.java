package com.example.newswebsite.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "API Trang chủ", description = "Này thử api thôi với phân quyền thôi")
public class HomeController {

    @GetMapping("/")
    @Operation(summary = "Trang chủ", description = "Trả về thông điệp chào mừng đến NewsWebsite")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ")
    })
    public String home() {
        return "Chào mừng đến với NewsWebsite!";
    }
}