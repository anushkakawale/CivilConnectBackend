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
public class ComplaintImageController {

    private final ComplaintImageService imageService;

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        Resource image = imageService.loadImage(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // Serves as JPEG, browsers handle PNG too usually
                .body(image);
    }
}
