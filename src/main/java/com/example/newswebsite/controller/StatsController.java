package com.example.newswebsite.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.newswebsite.domain.response.WebsiteStatsDTO;
import com.example.newswebsite.service.StatsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<WebsiteStatsDTO> getWebsiteStats() {
        log.info("Getting website statistics");
        WebsiteStatsDTO stats = statsService.getWebsiteStats();
        return ResponseEntity.ok(stats);
    }
}