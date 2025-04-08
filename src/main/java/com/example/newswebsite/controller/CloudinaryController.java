package com.example.newswebsite.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CloudinaryController {
    private final Cloudinary cloudinary;

    @PostMapping("/upload-image")
    public ResponseEntity<Map> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String url = (String) uploadResult.get("secure_url");
            return ResponseEntity.ok(Map.of("uploaded", true, "url", url));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("uploaded", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/export-html")
    public ResponseEntity<byte[]> exportHtml(@RequestBody Map<String, String> payload) {
        String content = payload.get("content");
        String html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Bài viết</title></head><body>"
                + content + "</body></html>";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.setContentDispositionFormData("attachment", "article.html");

        return new ResponseEntity<>(html.getBytes(), headers, HttpStatus.OK);
    }
}
