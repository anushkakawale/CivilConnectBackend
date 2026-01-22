package com.example.CivicConnect.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintImage;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.UploadedBy;
import com.example.CivicConnect.repository.ComplaintImageRepository;
import com.example.CivicConnect.repository.ComplaintRepository;

@Service
public class ComplaintImageService {

    private static final String UPLOAD_DIR = "uploads/";

    private final ComplaintRepository complaintRepository;
    private final ComplaintImageRepository complaintImageRepository;

    public ComplaintImageService(
            ComplaintRepository complaintRepository,
            ComplaintImageRepository complaintImageRepository) {
        this.complaintRepository = complaintRepository;
        this.complaintImageRepository = complaintImageRepository;
    }

    public void uploadImage(
            Long complaintId,
            MultipartFile file,
            User user) {

        try {
            Complaint complaint = complaintRepository.findById(complaintId)
                    .orElseThrow(() -> new RuntimeException("Complaint not found"));

            // 📂 Ensure folder exists
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            // 🧾 Unique filename
            String fileName =
                    System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, file.getBytes());

            // 🌐 Public path
            String imageUrl = "/uploads/" + fileName;

            // 🖼 Save image metadata
            ComplaintImage image = new ComplaintImage();
            image.setComplaint(complaint);
            image.setImageUrl(imageUrl);
            image.setLatitude(complaint.getLatitude());
            image.setLongitude(complaint.getLongitude());
            image.setUploadedAt(LocalDateTime.now());

            image.setUploadedBy(
                    user.getRole().name().contains("OFFICER")
                            ? UploadedBy.DEPARTMENT_OFFICER
                            : UploadedBy.CITIZEN
            );

            complaintImageRepository.save(image);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image");
        }
    }
}