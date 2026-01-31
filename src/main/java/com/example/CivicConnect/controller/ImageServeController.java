package com.example.CivicConnect.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.service.ComplaintImageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageServeController {

    private final ComplaintImageService imageService;

    @GetMapping("/{complaintId}/{fileName:.+}")
    public ResponseEntity<Resource> getImage(
            @PathVariable Long complaintId,
            @PathVariable String fileName) {

        Resource image = imageService.loadImage(complaintId, fileName);

        MediaType mediaType = resolveMediaType(fileName);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(image);
    }

    // ✅ Supports ALL common image types
    private MediaType resolveMediaType(String fileName) {

        String lower = fileName.toLowerCase();

        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        if (lower.endsWith(".bmp")) {
            return MediaType.parseMediaType("image/bmp");
        }

        return MediaType.IMAGE_JPEG;
    }
}
