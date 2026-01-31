package com.example.CivicConnect.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintImage;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ImageStage;
import com.example.CivicConnect.repository.ComplaintImageRepository;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.service.FileStorageService;

import lombok.RequiredArgsConstructor;
/*
@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintImageController {

    private final ComplaintImageService imageService;

    private final com.example.CivicConnect.repository.ComplaintRepository complaintRepository;
    private final com.example.CivicConnect.repository.ComplaintImageRepository imageRepository;
    private final com.example.CivicConnect.service.FileStorageService fileStorageService;

    @org.springframework.web.bind.annotation.PostMapping(value = "/{complaintId}/images", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @PathVariable Long complaintId,
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @org.springframework.web.bind.annotation.RequestParam(value = "type", required = false, defaultValue = "AFTER_RESOLUTION") String type,
            @org.springframework.web.bind.annotation.RequestParam(value = "latitude", required = false) Double latitude,
            @org.springframework.web.bind.annotation.RequestParam(value = "longitude", required = false) Double longitude,
            org.springframework.security.core.Authentication auth) {

        com.example.CivicConnect.entity.core.User user = (com.example.CivicConnect.entity.core.User) auth.getPrincipal();

        com.example.CivicConnect.entity.complaint.Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // Store file
        String filePath = fileStorageService.storeFile(file, complaintId);
        
        // Parse imageStage from string
        com.example.CivicConnect.entity.enums.ImageStage imageStage;
        try {
            imageStage = com.example.CivicConnect.entity.enums.ImageStage.valueOf(type.toUpperCase());
        } catch (Exception e) {
            imageStage = com.example.CivicConnect.entity.enums.ImageStage.AFTER_RESOLUTION;
        }
        
        com.example.CivicConnect.entity.complaint.ComplaintImage image = com.example.CivicConnect.entity.complaint.ComplaintImage.builder()
                .complaint(complaint)
                .imageUrl(filePath)
                .uploadedBy(user)
                .imageStage(imageStage)
                .latitude(latitude != null ? latitude : 0.0)
                .longitude(longitude != null ? longitude : 0.0)
                .build();

        imageRepository.save(image);

        return ResponseEntity.ok(java.util.Map.<String, Object>of(
            "message", "Image uploaded successfully",
            "imageUrl", filePath,
            "complaintId", complaintId
        ));
    }

    /**
     * Get all images for a complaint
     
    @GetMapping("/{complaintId}/images")
    public ResponseEntity<?> getComplaintImages(@PathVariable Long complaintId) {
        java.util.List<com.example.CivicConnect.entity.complaint.ComplaintImage> images = 
            imageRepository.findByComplaint_ComplaintId(complaintId);
        
        java.util.List<java.util.Map<String, Object>> imageList = images.stream()
            .map(img -> java.util.Map.<String, Object>of(
                "id", img.getImageId(),
                "imageUrl", "/api/images/" + img.getImageUrl(),
                "imageStage", img.getImageStage().name(),
                "uploadedBy", img.getUploadedBy().getName(),
                "uploadedAt", img.getUploadedAt(),
                "latitude", img.getLatitude(),
                "longitude", img.getLongitude()
            ))
            .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(imageList);
    }
}

// Separate controller for serving images
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
class ImageServeController {
    
    private final ComplaintImageService imageService;
    
    /**
     * Serve image file from local storage
  
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        Resource image = imageService.loadImage(fileName);
        
        // Detect content type
        String contentType = "image/jpeg";
        if (fileName.toLowerCase().endsWith(".png")) {
            contentType = "image/png";
        } else if (fileName.toLowerCase().endsWith(".gif")) {
            contentType = "image/gif";
        }
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(image);
    }
}
*/

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintImageController {

    private final ComplaintRepository complaintRepository;
    private final ComplaintImageRepository imageRepository;
    private final FileStorageService fileStorageService;

    // 🔐 UPLOAD IMAGE
    @PostMapping(value = "/{complaintId}/images", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long complaintId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(defaultValue = "AFTER_RESOLUTION") String stage,
            Authentication auth) {

        User user = (User) auth.getPrincipal();

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        String fileName = fileStorageService.storeComplaintImage(file, complaintId);

        ComplaintImage image = ComplaintImage.builder()
                .complaint(complaint)
                .imageUrl(fileName)
                .uploadedBy(user)
                .imageStage(ImageStage.valueOf(stage.toUpperCase()))
                .latitude(0.0)
                .longitude(0.0)
                .build();

        imageRepository.save(image);

        return ResponseEntity.ok(Map.of(
                "message", "Image uploaded",
                "imageUrl", "/api/images/" + complaintId + "/" + fileName
        ));
    }

    // 🔐 LIST IMAGE METADATA
    @GetMapping("/{complaintId}/images")
    public ResponseEntity<?> listImages(@PathVariable Long complaintId) {

        List<Map<String, Object>> images =
                imageRepository.findByComplaint_ComplaintIdOrderByUploadedAtAsc(complaintId)
                        .stream()
                        .map(img -> Map.<String, Object>of(
                                "imageId", img.getImageId(),
                                "stage", img.getImageStage().name(),
                                "uploadedBy", img.getUploadedBy().getName(),
                                "uploadedAt", img.getUploadedAt(),
                                "url", "/api/images/" +
                                        complaintId + "/" + img.getImageUrl()
                        ))
                        .toList();

        return ResponseEntity.ok(images);
    }
}