//package com.example.CivicConnect.service.citizen;
//
//import java.util.List;
//
//import org.springframework.stereotype.Service;
//
//import com.example.CivicConnect.dto.citizen.CitizenComplaintDTO;
//import com.example.CivicConnect.dto.citizen.CitizenDashboardSummaryDTO;
//import com.example.CivicConnect.dto.citizen.NotificationDTO;
//import com.example.CivicConnect.dto.citizen.WardComplaintDTO;
//import com.example.CivicConnect.entity.core.User;
//import com.example.CivicConnect.entity.enums.ComplaintStatus;
//import com.example.CivicConnect.entity.profiles.CitizenProfile;
//import com.example.CivicConnect.repository.CitizenProfileRepository;
//import com.example.CivicConnect.repository.ComplaintRepository;
//import com.example.CivicConnect.repository.NotificationRepository;
//import com.example.CivicConnect.repository.UserRepository;
//
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class CitizenDashboardService {
//
//    private final UserRepository userRepository;
//    private final CitizenProfileRepository profileRepository;
//    private final ComplaintRepository complaintRepository;
//    private final NotificationRepository notificationRepository;
//
//    // ================= DASHBOARD SUMMARY =================
//    public CitizenDashboardSummaryDTO getSummary(String email) {
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() ->
//                        new RuntimeException("User not found with email: " + email));
//
//        CitizenProfile profile = profileRepository.findByUser(user)
//                .orElseThrow(() ->
//                        new RuntimeException("Citizen profile not found"));
//
//        CitizenDashboardSummaryDTO dto = new CitizenDashboardSummaryDTO();
//        dto.setCitizenName(user.getName());
//        dto.setWardName(profile.getWard().getAreaName());
//
//        dto.setTotalComplaints(
//                complaintRepository.countByCitizenUserId(user.getId()));
//
//        dto.setInProgress(
//                complaintRepository.countByCitizenUserIdAndStatus(
//                        user.getId(), ComplaintStatus.IN_PROGRESS));
//
//        dto.setResolved(
//                complaintRepository.countByCitizenUserIdAndStatus(
//                        user.getId(), ComplaintStatus.COMPLETED));
//
//        dto.setPendingApproval(
//                complaintRepository.countByCitizenUserIdAndStatus(
//                        user.getId(), ComplaintStatus.ASSIGNED));
//
//        return dto;
//    }
//
//    // ================= MY COMPLAINTS =================
//    public List<CitizenComplaintDTO> getMyComplaints(String email) {
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        return complaintRepository
//                .findByCitizenUserIdOrderByCreatedAtDesc(user.getId())
//                .stream()
//                .map(c -> {
//                    CitizenComplaintDTO dto = new CitizenComplaintDTO();
//                    dto.setComplaintId(c.getId());
//                    dto.setTitle(c.getTitle());
//                    dto.setDepartmentName(c.getDepartment().getName());
//                    dto.setStatus(c.getStatus().name());
//                    dto.setDuplicateCount(c.getDuplicateCount());
//                    dto.setCreatedAt(c.getCreatedAt().toString());
//                    return dto;
//                })
//                .toList();
//    }
//
//    // ================= WARD COMPLAINTS =================
//    public List<WardComplaintDTO> getWardComplaints(String email) {
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        CitizenProfile profile = profileRepository.findByUser(user)
//                .orElseThrow(() -> new RuntimeException("Profile not found"));
//
//        return complaintRepository.findByWardId(profile.getWard().getId())
//                .stream()
//                .map(c -> {
//                    WardComplaintDTO dto = new WardComplaintDTO();
//                    dto.setComplaintId(c.getId());
//                    dto.setTitle(c.getTitle());
//                    dto.setDepartmentName(c.getDepartment().getName());
//                    dto.setStatus(c.getStatus().name());
//                    dto.setDuplicateCount(c.getDuplicateCount());
//                    return dto;
//                })
//                .toList();
//    }
//
//    // ================= NOTIFICATIONS =================
//    public List<NotificationDTO> getNotifications(String email) {
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        return notificationRepository
//                .findByUserIdOrderByCreatedAtDesc(user.getId())
//                .stream()
//                .map(n -> {
//                    NotificationDTO dto = new NotificationDTO();
//                    dto.setId(n.getId());
//                    dto.setMessage(n.getMessage());
//                    dto.setSeen(n.getSeen());
//                    dto.setCreatedAt(n.getCreatedAt().toString());
//                    return dto;
//                })
//                .toList();
//    }
//}
