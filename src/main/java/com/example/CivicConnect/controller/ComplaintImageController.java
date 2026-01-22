package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.ComplaintImageService;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintImageController {

    private final ComplaintImageService complaintImageService;

    public ComplaintImageController(ComplaintImageService complaintImageService) {
        this.complaintImageService = complaintImageService;
    }

    //  UPLOAD IMAGE FOR COMPLAINT
    @PostMapping("/{complaintId}/images")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long complaintId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        complaintImageService.uploadImage(complaintId, file, user);

        return ResponseEntity.ok("Image uploaded successfully");
    }
}
