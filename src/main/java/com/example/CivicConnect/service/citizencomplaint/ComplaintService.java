package com.example.CivicConnect.service.citizencomplaint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.dto.ComplaintRequestDTO;
import com.example.CivicConnect.dto.ComplaintResponseDTO;
import com.example.CivicConnect.dto.ComplaintSummaryDTO;
import com.example.CivicConnect.dto.ComplaintTrackingDTO;
import com.example.CivicConnect.dto.StatusHistoryDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.geography.Department;
import com.example.CivicConnect.entity.geography.Ward;
import com.example.CivicConnect.entity.profiles.CitizenProfile;
import com.example.CivicConnect.entity.system.Notification;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;
import com.example.CivicConnect.repository.DepartmentRepository;
import com.example.CivicConnect.repository.NotificationRepository;

@Service
@Transactional
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final CitizenProfileRepository citizenProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final ComplaintAssignmentService assignmentService;
    private final NotificationRepository notificationRepository;
    private final ComplaintStatusHistoryRepository historyRepository;

    public ComplaintService(
            ComplaintRepository complaintRepository,
            CitizenProfileRepository citizenProfileRepository,
            DepartmentRepository departmentRepository,
            ComplaintAssignmentService assignmentService,
            NotificationRepository notificationRepository,
            ComplaintStatusHistoryRepository historyRepository) {

        this.complaintRepository = complaintRepository;
        this.citizenProfileRepository = citizenProfileRepository;
        this.departmentRepository = departmentRepository;
        this.assignmentService = assignmentService;
        this.notificationRepository = notificationRepository;
        this.historyRepository = historyRepository;
    }

    // ✅ REGISTER COMPLAINT (JWT SAFE)
    public ComplaintResponseDTO registerComplaint(
            ComplaintRequestDTO request,
            User citizen) {

        CitizenProfile citizenProfile =
                citizenProfileRepository
                        .findByUser_UserId(citizen.getUserId())
                        .orElseThrow(() -> new RuntimeException("Citizen profile not found"));

        Ward ward = citizenProfile.getWard();

        Department department =
                departmentRepository
                        .findById(request.getDepartmentId())
                        .orElseThrow(() -> new RuntimeException("Department not found"));

        LocalDateTime duplicateWindow = LocalDateTime.now().minusHours(24);

        Optional<Complaint> duplicate =
                complaintRepository
                        .findByWard_WardIdAndDepartment_DepartmentIdAndTitleIgnoreCaseAndCreatedAtAfter(
                                ward.getWardId(),
                                department.getDepartmentId(),
                                request.getTitle(),
                                duplicateWindow
                        );

        // 🔁 DUPLICATE FOUND
        if (duplicate.isPresent()) {

            Complaint existing = duplicate.get();
            existing.setDuplicateCount(existing.getDuplicateCount() + 1);

            notifyUser(
                    citizen,
                    "Your complaint was linked to Complaint ID: " + existing.getComplaintId()
            );

            return new ComplaintResponseDTO(
                    existing.getComplaintId(),
                    existing.getStatus().name(),
                    existing.getDuplicateCount(),
                    "Duplicate complaint linked"
            );
        }

        // 🆕 CREATE NEW COMPLAINT
        Complaint complaint = new Complaint();
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setLatitude(request.getLatitude());
        complaint.setLongitude(request.getLongitude());
        complaint.setCitizen(citizen);
        complaint.setWard(ward);
        complaint.setDepartment(department);
        complaint.setStatus(ComplaintStatus.SUBMITTED);
        complaint.setDuplicateCount(1);
        complaint.setCreatedAt(LocalDateTime.now());

        complaintRepository.save(complaint);

        logStatus(complaint, ComplaintStatus.SUBMITTED, citizen);

        assignmentService.assignOfficer(complaint);

        notifyUser(
                citizen,
                "Complaint registered successfully. ID: " + complaint.getComplaintId()
        );

        return new ComplaintResponseDTO(
                complaint.getComplaintId(),
                complaint.getStatus().name(),
                complaint.getDuplicateCount(),
                "Complaint registered"
        );
    }

    // 📄 VIEW ALL COMPLAINTS (Citizen Dashboard)
    public List<ComplaintSummaryDTO> viewCitizenComplaints(Long citizenUserId) {

        return complaintRepository
                .findByCitizen_UserIdOrderByCreatedAtDesc(citizenUserId)
                .stream()
                .map(c -> {
                    ComplaintSummaryDTO dto = new ComplaintSummaryDTO();
                    dto.setComplaintId(c.getComplaintId());
                    dto.setTitle(c.getTitle());
                    dto.setStatus(c.getStatus());
                    dto.setCreatedAt(c.getCreatedAt());
                    return dto;
                })
                .toList();
    }

    // 🔍 TRACK SINGLE COMPLAINT
    public ComplaintTrackingDTO trackComplaint(Long complaintId, Long citizenUserId) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (!complaint.getCitizen().getUserId().equals(citizenUserId)) {
            throw new RuntimeException("Access denied");
        }

        List<StatusHistoryDTO> history =
                historyRepository
                        .findByComplaint_ComplaintIdOrderByChangedAtAsc(complaintId)
                        .stream()
                        .map(h -> {
                            StatusHistoryDTO dto = new StatusHistoryDTO();
                            dto.setStatus(h.getStatus());
                            dto.setChangedAt(h.getChangedAt());
                            dto.setChangedBy(
                                    h.getChangedBy() != null
                                            ? h.getChangedBy().getName()
                                            : "SYSTEM"
                            );
                            return dto;
                        })
                        .toList();

        ComplaintTrackingDTO dto = new ComplaintTrackingDTO();
        dto.setComplaintId(complaint.getComplaintId());
        dto.setTitle(complaint.getTitle());
        dto.setDescription(complaint.getDescription());
        dto.setCurrentStatus(complaint.getStatus());
        dto.setHistory(history);

        return dto;
    }

    // 🔐 STATUS HISTORY
    private void logStatus(Complaint complaint, ComplaintStatus status, User user) {
        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setStatus(status);
        history.setChangedBy(user);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    // 🔔 NOTIFICATION
    private void notifyUser(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setSeen(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
