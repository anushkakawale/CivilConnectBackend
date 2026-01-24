package com.example.CivicConnect.service;
//


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

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
    private final ComplaintImageRepository imageRepository;

    public ComplaintImageService(
            ComplaintRepository complaintRepository,
            ComplaintImageRepository imageRepository) {
        this.complaintRepository = complaintRepository;
        this.imageRepository = imageRepository;
    }

    // 📸 Upload image
    public void uploadImage(Long complaintId, MultipartFile file, User user) {

        try {
            Complaint complaint = complaintRepository.findById(complaintId)
                    .orElseThrow(() -> new RuntimeException("Complaint not found"));

            Files.createDirectories(Paths.get(UPLOAD_DIR));

            String fileName = System.currentTimeMillis()
                    + "_" + file.getOriginalFilename();

            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.write(path, file.getBytes());

            ComplaintImage image = new ComplaintImage();
            image.setComplaint(complaint);
            image.setImageUrl("/uploads/" + fileName);
            image.setLatitude(complaint.getLatitude());
            image.setLongitude(complaint.getLongitude());
            image.setUploadedAt(LocalDateTime.now());

            if (user.getRole().name().contains("OFFICER")) {
                image.setUploadedBy(UploadedBy.DEPARTMENT_OFFICER);
            } else {
                image.setUploadedBy(UploadedBy.CITIZEN);
            }

            imageRepository.save(image);

        } catch (IOException e) {
            throw new RuntimeException("Image upload failed", e);
        }
    }

    // 👀 View images (ALL ROLES)
    public List<ComplaintImage> viewWorkImages(
            Long complaintId,
            User viewer) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        // Citizen can see only their complaint
        if (viewer.getRole().name().equals("CITIZEN") &&
            !complaint.getCitizen().getUserId().equals(viewer.getUserId())) {
            throw new RuntimeException("Access denied");
        }

        return imageRepository.findByComplaintOrderByUploadedAtAsc(complaint);
    }
}


//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.example.CivicConnect.entity.complaint.Complaint;
//import com.example.CivicConnect.entity.complaint.ComplaintImage;
//import com.example.CivicConnect.entity.core.User;
//import com.example.CivicConnect.entity.enums.ComplaintStatus;
//import com.example.CivicConnect.entity.enums.UploadedBy;
//import com.example.CivicConnect.repository.ComplaintImageRepository;
//import com.example.CivicConnect.repository.ComplaintRepository;
//
//@Service
//public class ComplaintImageService {
//
//    private static final String UPLOAD_DIR = "uploads/";
//
//    private final ComplaintRepository complaintRepository;
//    private final ComplaintImageRepository imageRepository;
//
//    public ComplaintImageService(
//            ComplaintRepository complaintRepository,
//            ComplaintImageRepository imageRepository) {
//        this.complaintRepository = complaintRepository;
//        this.imageRepository = imageRepository;
//    }
//
//    // =====================================================
//    // UPLOAD IMAGE (Citizen OR Officer)
//    // =====================================================
//    public void uploadImage(
//            Long complaintId,
//            MultipartFile file,
//            User user) {
//
//        try {
//            Complaint complaint = complaintRepository.findById(complaintId)
//                    .orElseThrow(() -> new RuntimeException("Complaint not found"));
//
//            // 📂 Ensure folder exists
//            Files.createDirectories(Paths.get(UPLOAD_DIR));
//
//            String fileName =
//                    System.currentTimeMillis() + "_" + file.getOriginalFilename();
//
//            Path filePath = Paths.get(UPLOAD_DIR + fileName);
//            Files.write(filePath, file.getBytes());
//
//            String imageUrl = "/uploads/" + fileName;
//
//            ComplaintImage image = new ComplaintImage();
//            image.setComplaint(complaint);
//            image.setImageUrl(imageUrl);
//            image.setLatitude(complaint.getLatitude());
//            image.setLongitude(complaint.getLongitude());
//            image.setUploadedAt(LocalDateTime.now());
//
//            // 🔐 WHO UPLOADED?
//            if (user.getRole().name().contains("OFFICER")) {
//
//                // ✅ Officer can upload only when work started
//                if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS &&
//                    complaint.getStatus() != ComplaintStatus.RESOLVED) {
//                    throw new RuntimeException(
//                            "Work images allowed only after IN_PROGRESS");
//                }
//
//                image.setUploadedBy(UploadedBy.DEPARTMENT_OFFICER);
//            } else {
//                image.setUploadedBy(UploadedBy.CITIZEN);
//            }
//
//            imageRepository.save(image);
//
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to upload image");
//        }
//    }
//
//    // =====================================================
//    // VIEW WORK IMAGES (ADMIN / WARD / DEPT OFFICER)
//    // =====================================================
//    public List<ComplaintImage> viewWorkImages(
//            Long complaintId,
//            User viewer) {
//
//        Complaint complaint = complaintRepository.findById(complaintId)
//                .orElseThrow(() -> new RuntimeException("Complaint not found"));
//
//        switch (viewer.getRole()) {
//
//            case ADMIN:
//                return imageRepository.findByComplaintAndUploadedByIn(
//                        complaint,
//                        List.of(
//                                UploadedBy.DEPARTMENT_OFFICER,
//                                UploadedBy.WARD_OFFICER
//                        )
//                );
//
//            case DEPARTMENT_OFFICER:
//                if (!complaint.getAssignedOfficer()
//                        .getUserId().equals(viewer.getUserId())) {
//                    throw new RuntimeException("Access denied");
//                }
//                return imageRepository.findByComplaintAndUploadedBy(
//                        complaint,
//                        UploadedBy.DEPARTMENT_OFFICER
//                );
//
//            case WARD_OFFICER:
//                return imageRepository.findByComplaintAndUploadedByIn(
//                        complaint,
//                        List.of(
//                                UploadedBy.DEPARTMENT_OFFICER,
//                                UploadedBy.WARD_OFFICER
//                        )
//                );
//
//            default:
//                throw new RuntimeException("Access denied");
////        }
//    }
//}
