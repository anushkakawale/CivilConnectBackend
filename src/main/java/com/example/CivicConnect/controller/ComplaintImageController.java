package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ImageStage;
import com.example.CivicConnect.repository.UserRepository;
import com.example.CivicConnect.service.ComplaintImageService;
@RestController
@RequestMapping("/api/complaints")
public class ComplaintImageController {

    private final ComplaintImageService imageService;
    private final UserRepository userRepository;

    public ComplaintImageController(
            ComplaintImageService imageService,
            UserRepository userRepository) {
        this.imageService = imageService;
        this.userRepository = userRepository;
    }

    @PostMapping("/{complaintId}/images")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long complaintId,
            @RequestParam MultipartFile file,
            @RequestParam ImageStage stage,
            Authentication auth) {

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        imageService.uploadImage(complaintId, file, stage, user);
        return ResponseEntity.ok("Image uploaded");
    }
    @GetMapping("/{complaintId}/images")
    public ResponseEntity<?> viewImages(
            @PathVariable Long complaintId,
            Authentication auth) {

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                imageService.viewWorkImages(complaintId, user)
        );
    }
}
