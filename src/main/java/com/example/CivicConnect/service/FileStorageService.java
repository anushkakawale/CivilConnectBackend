package com.example.CivicConnect.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
/*
@Service
public class FileStorageService {

    @Value("${file.upload.dir}")
    private String uploadDir;

    /**
     * Store a file in the complaint-specific directory structure
     * @param file The multipart file to store
     * @param complaintId The complaint ID for directory organization
     * @return The relative file path (e.g., "uploads/complaints/123/uuid-filename.jpg")

    public String storeFile(MultipartFile file, Long complaintId) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Cannot store empty file");
            }

            // Validate file type (images only)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed");
            }

            // Validate file size (max 5MB)
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSize) {
                throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
            }

            // Create complaint-specific directory
            Path complaintDir = Paths.get(uploadDir, complaintId.toString());
            Files.createDirectories(complaintDir);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Store file
            Path targetLocation = complaintDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path for URL construction
            return uploadDir + "/" + complaintId + "/" + uniqueFilename;

        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file: " + ex.getMessage(), ex);
        }
    }

    /**
     * Delete a file from storage
     * @param filePath The relative file path to delete
    
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete file: " + ex.getMessage(), ex);
        }
    }

    /**
     * Get the full path for a file
     * @param relativePath The relative path
     * @return The full Path object
    
    public Path getFilePath(String relativePath) {
        return Paths.get(relativePath);
    }
}
*/

@Service
public class FileStorageService {

    @Value("${file.upload.dir}")
    private String uploadDir;

    public String storeComplaintImage(MultipartFile file, Long complaintId) {

        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Empty file");
            }

            if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
                throw new RuntimeException("Only image files allowed");
            }

            Path dir = Paths.get(uploadDir, "complaints", complaintId.toString());
            Files.createDirectories(dir);

            String original = file.getOriginalFilename();
            String ext = original.substring(original.lastIndexOf("."));
            String fileName = UUID.randomUUID() + ext;

            Files.copy(
                    file.getInputStream(),
                    dir.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING
            );

            // ✅ store ONLY filename in DB
            return fileName;

        } catch (Exception e) {
            throw new RuntimeException("Failed to store image", e);
        }
    }
}