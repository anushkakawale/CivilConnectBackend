package com.example.CivicConnect.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.CivicConnect.dto.ComplaintImageDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintImage;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.ImageStage;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.enums.UploadedBy;
import com.example.CivicConnect.repository.ComplaintImageRepository;
import com.example.CivicConnect.repository.ComplaintRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ComplaintImageService {

    private static final String UPLOAD_DIR = "uploads";

    private final ComplaintRepository complaintRepository;
    private final ComplaintImageRepository imageRepository;

    public ComplaintImageService(
            ComplaintRepository complaintRepository,
            ComplaintImageRepository imageRepository) {
        this.complaintRepository = complaintRepository;
        this.imageRepository = imageRepository;
    }

    // ===============================
    // UPLOAD IMAGE
    // ===============================
    public void uploadImage(
            Long complaintId,
            MultipartFile file,
            ImageStage stage,
            User user) {

        try {
            Complaint complaint = complaintRepository.findById(complaintId)
                    .orElseThrow(() -> new RuntimeException("Complaint not found"));

            // 🔐 Citizen → only own complaint
            if (user.getRole() == RoleName.CITIZEN &&
                !complaint.getCitizen().getUserId().equals(user.getUserId())) {
                throw new RuntimeException("Access denied");
            }

            // 🔐 Officer → only after work starts
            if (user.getRole().name().contains("OFFICER") &&
                complaint.getStatus() == ComplaintStatus.ASSIGNED) {
                throw new RuntimeException("Start work before uploading images");
            }

            // 📁 Ensure directory exists
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
            Files.createDirectories(uploadPath);

            String fileName =
                    System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, file.getBytes());

            ComplaintImage image = new ComplaintImage();
            image.setComplaint(complaint);
            image.setImageUrl("/uploads/" + fileName);
            image.setImageStage(stage);
            image.setLatitude(complaint.getLatitude());
            image.setLongitude(complaint.getLongitude());
            image.setUploadedAt(LocalDateTime.now());

            image.setUploadedBy(
                    user.getRole().name().contains("OFFICER")
                            ? UploadedBy.DEPARTMENT_OFFICER
                            : UploadedBy.CITIZEN
            );

            imageRepository.save(image);

        } catch (IOException e) {
            throw new RuntimeException("Image upload failed", e);
        }
    }

    // ===============================
    // VIEW IMAGES
    // ===============================
    public List<ComplaintImage> viewWorkImages(
            Long complaintId,
            User user) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // citizen → must own complaint
        if (user.getRole() == RoleName.CITIZEN &&
            !complaint.getCitizen().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Access denied");
        }

        return imageRepository
                .findByComplaint_ComplaintIdOrderByUploadedAtAsc(complaintId);
    }

//    public List<ComplaintImageDTO> viewImages(Long complaintId, User viewer) {
//
//        Complaint complaint = complaintRepository.findById(complaintId)
//                .orElseThrow(() -> new RuntimeException("Complaint not found"));
//
//        if (viewer.getRole() == RoleName.CITIZEN &&
//            !complaint.getCitizen().getUserId().equals(viewer.getUserId())) {
//            throw new RuntimeException("Access denied");
//        }
//
//        return imageRepository
//                .findByComplaint_ComplaintIdOrderByUploadedAtAsc(complaintId)
//                .stream()
//                .map(img -> new ComplaintImageDTO(
//                        img.getImageUrl(),
//                        img.getImageStage().name(),
//                        img.getUploadedAt()
//                ))
//                .toList();
//    }
}
