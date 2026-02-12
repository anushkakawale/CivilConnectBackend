package com.example.CivicConnect.service.citizencomplaint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.dto.AuditLogDTO;
import com.example.CivicConnect.dto.ComplaintRequestDTO;
import com.example.CivicConnect.dto.ComplaintResponseDTO;
import com.example.CivicConnect.dto.ComplaintSummaryDTO;
import com.example.CivicConnect.dto.ComplaintTrackingDTO;
import com.example.CivicConnect.dto.StatusHistoryDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintReport;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.NotificationType;
import com.example.CivicConnect.entity.enums.Priority;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.entity.geography.Department;
import com.example.CivicConnect.entity.geography.Ward;
import com.example.CivicConnect.entity.profiles.CitizenProfile;
import com.example.CivicConnect.entity.sla.ComplaintSla;
import com.example.CivicConnect.repository.ComplaintFeedbackRepository;
import com.example.CivicConnect.repository.AccessLogRepository;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.ComplaintImageRepository;
import com.example.CivicConnect.repository.ComplaintReportRepository;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintSlaRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;
import com.example.CivicConnect.repository.DepartmentRepository;
import com.example.CivicConnect.service.FileStorageService;
import com.example.CivicConnect.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final CitizenProfileRepository citizenProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final ComplaintAssignmentService assignmentService;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final ComplaintSlaRepository slaRepository;
    private final NotificationService notificationService;
    private final ComplaintReportRepository complaintReportRepository;
    private final ComplaintImageRepository imageRepository;
    private final FileStorageService fileStorageService;
    private final ComplaintFeedbackRepository feedbackRepository;
    private final AccessLogRepository accessLogRepository;

//    public ComplaintService(
//            ComplaintRepository complaintRepository,
//            CitizenProfileRepository citizenProfileRepository,
//            DepartmentRepository departmentRepository,
//            ComplaintAssignmentService assignmentService,
//            ComplaintStatusHistoryRepository historyRepository,
//            ComplaintSlaRepository slaRepository,
//            NotificationService notificationService,
//            ComplaintReportRepository complaintReportRepository) {
//
//        this.complaintRepository = complaintRepository;
//        this.citizenProfileRepository = citizenProfileRepository;
//        this.departmentRepository = departmentRepository;
//        this.assignmentService = assignmentService;
//        this.historyRepository = historyRepository;
//        this.slaRepository = slaRepository;
//        this.notificationService = notificationService;
//        this.complaintReportRepository = complaintReportRepository;
//    }


    public ComplaintResponseDTO registerComplaint(
            ComplaintRequestDTO request,
            User citizen) {
        return registerComplaintWithImages(request, citizen, null);
    }

    // Register complaint with images (NEW & EFFICIENT)
    public ComplaintResponseDTO registerComplaintWithImages(
            ComplaintRequestDTO request,
            User citizen,
            org.springframework.web.multipart.MultipartFile[] images) {

        CitizenProfile profile = citizenProfileRepository
                .findByUser_UserId(citizen.getUserId())
                .orElseThrow(() -> new RuntimeException("Citizen profile not found"));

        if (profile.getWard() == null) {
            throw new RuntimeException("Ward not set. Please update your profile.");
        }

        Ward ward = profile.getWard();
        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // Duplicate check (24 hours)
        Optional<Complaint> duplicate =
                complaintRepository.findByWard_WardIdAndDepartment_DepartmentIdAndTitleIgnoreCaseAndCreatedAtAfter(
                        ward.getWardId(),
                        department.getDepartmentId(),
                        request.getTitle(),
                        LocalDateTime.now().minusHours(24)
                );

        if (duplicate.isPresent()) {
            Complaint existing = duplicate.get();
            if (complaintReportRepository.existsByComplaintAndCitizen_UserId(existing, citizen.getUserId())) {
                throw new RuntimeException("You have already reported this complaint.");
            }

            // Save additional report for duplicate
            createReport(existing, citizen, request);
            existing.setDuplicateCount(existing.getDuplicateCount() + 1);
            complaintRepository.save(existing);

            notificationService.notifyCitizen(citizen, "Duplicate Complaint", "A similar complaint already exists.", existing.getComplaintId(), NotificationType.COMPLAINT_CREATED);

            return new ComplaintResponseDTO(existing.getComplaintId(), existing.getStatus().name(), existing.getDuplicateCount(), "Reported as duplicate of #" + existing.getComplaintId());
        }

        // 🆕 NEW COMPLAINT
        Complaint complaint = new Complaint();
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setLatitude(request.getLatitude());
        complaint.setLongitude(request.getLongitude());
        complaint.setCitizen(citizen);
        complaint.setWard(ward);
        complaint.setDepartment(department);
        complaint.setStatus(ComplaintStatus.SUBMITTED);
        complaint.setPriority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM);
        complaint.setDuplicateCount(1);
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());
        complaint.setCreatedBy(citizen);
        complaint.setLastUpdatedBy(citizen);

        Complaint savedComplaint = complaintRepository.save(complaint);

        // 1️⃣ SAVE IMAGES (If any)
        if (images != null && images.length > 0) {
            for (org.springframework.web.multipart.MultipartFile file : images) {
                if (!file.isEmpty()) {
                    String fileName = fileStorageService.storeComplaintImage(file, savedComplaint.getComplaintId());
                    com.example.CivicConnect.entity.complaint.ComplaintImage img = new com.example.CivicConnect.entity.complaint.ComplaintImage();
                    img.setComplaint(savedComplaint);
                    img.setImageUrl(fileName);
                    img.setImageStage(com.example.CivicConnect.entity.enums.ImageStage.BEFORE_WORK);
                    img.setUploadedAt(LocalDateTime.now());
                    img.setUploadedBy(citizen);
                    imageRepository.save(img);
                }
            }
        }

        // 2️⃣ SAVE REPORT
        createReport(savedComplaint, citizen, request);

        // 3️⃣ SAVE SLA
        ComplaintSla sla = new ComplaintSla();
        sla.setComplaint(savedComplaint);
        sla.setSlaStartTime(LocalDateTime.now());
        sla.setSlaDeadline(LocalDateTime.now().plusHours(department.getSlaHours()));
        sla.setStatus(SLAStatus.ON_TRACK);
        slaRepository.save(sla);

        // 4️⃣ LOG HISTORY
        logStatus(savedComplaint, ComplaintStatus.SUBMITTED, citizen, false, "Initial submission");

        // 5️⃣ AUTO ASSIGN
        assignmentService.assignOfficer(savedComplaint);

        // 6️⃣ NOTIFY
        notificationService.notifyCitizen(citizen, "Complaint Registered", "Your complaint has been submitted successfully.", savedComplaint.getComplaintId(), NotificationType.COMPLAINT_CREATED);

        return new ComplaintResponseDTO(savedComplaint.getComplaintId(), savedComplaint.getStatus().name(), 1, "Registered successfully");
    }

    private void createReport(Complaint complaint, User citizen, ComplaintRequestDTO request) {
        ComplaintReport report = new ComplaintReport();
        report.setComplaint(complaint);
        report.setCitizen(citizen);
        report.setDescription(request.getDescription());
        report.setLatitude(request.getLatitude());
        report.setLongitude(request.getLongitude());
        report.setReportedAt(LocalDateTime.now());
        complaintReportRepository.save(report);
    }
    public Page<ComplaintSummaryDTO> viewCitizenComplaints(
            Long citizenUserId,
            Pageable pageable) {

        return complaintRepository
            .findByCitizen_UserIdOrderByCreatedAtDesc(citizenUserId, pageable)
            .map(c -> new ComplaintSummaryDTO(
                c.getComplaintId(),
                c.getTitle(),
                c.getStatus(),
                c.getCreatedAt()
            ));
    }

    // VIEW COMPLAINTS
    public List<ComplaintSummaryDTO> viewCitizenComplaints(Long citizenUserId) {

        return complaintRepository
                .findByCitizen_UserIdOrderByCreatedAtDesc(citizenUserId)
                .stream()
                .map(c -> new ComplaintSummaryDTO(
                        c.getComplaintId(),
                        c.getTitle(),
                        c.getStatus(),
                        c.getCreatedAt()
                ))
                .toList();
    }

    // TRACK COMPLAINT

    // ===============================
    // MAP VISUALIZATION
    // ===============================
    public List<com.example.CivicConnect.dto.ComplaintMapDTO> getComplaintsForMap() {
        // Fetch all complaints that are not CLOSED or REJECTED to show on map
        // Or show all depending on requirements. Assuming all for now.
        return complaintRepository.findAll()
                .stream()
                .map(c -> new com.example.CivicConnect.dto.ComplaintMapDTO(
                        c.getComplaintId(),
                        c.getLatitude(),
                        c.getLongitude(),
                        c.getStatus(),
                        null,
                        c.getTitle(),
                        c.getDescription(),
                        (c.getImages() != null && !c.getImages().isEmpty()) ? c.getImages().get(0).getImageUrl() : null,
                        c.getDepartment().getName(),
                        c.getWard().getAreaName(),
                        c.getPriority().name(),
                        c.getCreatedAt()
                ))
                .toList();
    }

    // SUBMIT FEEDBACK
    public void submitFeedback(Long complaintId, Long citizenUserId, Integer rating, String feedbackComments) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

//        if (!complaint.getCitizen().getUserId().equals(citizenUserId)) {
//            throw new RuntimeException("Access denied: You can only provide feedback for your own complaints");
//        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED && complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new RuntimeException("Feedback can only be provided for RESOLVED or CLOSED complaints");
        }

        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        // Check if user already rated
        if (feedbackRepository.existsByComplaint_ComplaintIdAndCitizen_UserId(complaintId, citizenUserId)) {
            throw new RuntimeException("You have already provided feedback for this complaint");
        }
        
        // Save new feedback
        com.example.CivicConnect.entity.complaint.ComplaintFeedback feedback = 
                com.example.CivicConnect.entity.complaint.ComplaintFeedback.builder()
                .complaint(complaint)
                .citizen(complaint.getCitizen()) // Assuming citizen is the rater
                .rating(rating)
                .comment(feedbackComments)
                .createdAt(LocalDateTime.now())
                .build();
        
        // Wait, if ANY citizen can rate, we need to fetch the rater user
        // The citizenUserId argument is the rater's ID.
        // Let's verify if the rater needs to be the complaint creator
        // User request: "many users of that wards can complaint" (rate?)
        // Let's assume ANY citizen can rate if they are in the ward, OR just allow any citizen.
        // For safety, let's just use the citizenUserId passed in.
        
        User rater = citizenProfileRepository.findByUser_UserId(citizenUserId)
                .orElseThrow(() -> new RuntimeException("Citizen not found"))
                .getUser();

        feedback.setCitizen(rater);
        feedbackRepository.save(feedback);

        // Update Aggregate Data on Complaint
        List<com.example.CivicConnect.entity.complaint.ComplaintFeedback> allFeedbacks = 
                feedbackRepository.findByComplaint_ComplaintIdOrderByCreatedAtDesc(complaintId);
        
        double avg = allFeedbacks.stream().mapToInt(com.example.CivicConnect.entity.complaint.ComplaintFeedback::getRating).average().orElse(0.0);
        int total = allFeedbacks.size();
        
        complaint.setAverageRating(Math.round(avg * 10.0) / 10.0); // Round to 1 decimal
        complaint.setTotalRatings(total);
        
        // Legacy fields (optional: keep latest or creator's)
        if (complaint.getCitizen().getUserId().equals(citizenUserId)) {
            complaint.setRating(rating);
            complaint.setFeedback(feedbackComments);
        }

        complaintRepository.save(complaint);
    }

    // REOPEN COMPLAINT
    public void reopenComplaint(Long complaintId, Long citizenUserId, String remarks) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (!complaint.getCitizen().getUserId().equals(citizenUserId)) {
            throw new RuntimeException("Access denied: You can only reopen your own complaints");
        }

        // Check if status is RESOLVED or CLOSED
        if (complaint.getStatus() != ComplaintStatus.RESOLVED && complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new RuntimeException("Only RESOLVED or CLOSED complaints can be reopened");
        }

        // Check 7-day window
        LocalDateTime referenceTime = complaint.getStatus() == ComplaintStatus.CLOSED ? 
                complaint.getClosedAt() : complaint.getUpdatedAt();
        
        if (referenceTime == null) referenceTime = complaint.getCreatedAt();

        if (LocalDateTime.now().isAfter(referenceTime.plusDays(7))) {
            throw new RuntimeException("Complaints can only be reopened within 7 days of resolution or closure");
        }

        // Reopen
        complaint.setStatus(ComplaintStatus.REOPENED);
        complaint.setUpdatedAt(LocalDateTime.now());
        complaint.setLastUpdatedBy(complaint.getCitizen());
        
        // Reset closure data if it was closed
        if (complaint.getStatus() == ComplaintStatus.CLOSED) {
            complaint.setClosedAt(null);
            complaint.setClosedByAdmin(null);
        }

        complaintRepository.save(complaint);

        // 🔁 RESTART SLA
        slaRepository.findByComplaint(complaint).ifPresent(sla -> {
            sla.setSlaStartTime(LocalDateTime.now());
            sla.setSlaDeadline(LocalDateTime.now().plusHours(
                    complaint.getDepartment().getSlaHours()
            ));
            sla.setStatus(com.example.CivicConnect.entity.enums.SLAStatus.ON_TRACK);
            sla.setEscalated(false);
            sla.setSlaBreached(false); // Reset breach status
            slaRepository.save(sla);
        });
        
        // Reset Complaint-level SLA flags
        complaint.setSlaBreached(false);
        complaint.setEscalated(false);

        // Log history
        logStatus(complaint, ComplaintStatus.REOPENED, complaint.getCitizen(), false, remarks);

        // Notify assigned officer
        if (complaint.getAssignedOfficer() != null) {
            notificationService.notifyOfficer(
                complaint.getAssignedOfficer(),
                "Complaint Reopened",
                "Complaint #" + complaintId + " has been reopened by the citizen: " + remarks,
                complaintId,
                NotificationType.STATUS_UPDATE
            );
        }
    }

    // HELPERS
    private void logStatus(
            Complaint complaint,
            ComplaintStatus status,
            User user,
            boolean systemGenerated) {
        logStatus(complaint, status, user, systemGenerated, null);
    }

    private void logStatus(
            Complaint complaint,
            ComplaintStatus status,
            User user,
            boolean systemGenerated,
            String remarks) {

        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setStatus(status);
        history.setChangedBy(user);
        history.setSystemGenerated(systemGenerated);
        history.setChangedAt(LocalDateTime.now());
        history.setRemarks(remarks != null ? remarks : (systemGenerated ? "System update" : "Status changed to " + status));

        historyRepository.save(history);
    }
}
