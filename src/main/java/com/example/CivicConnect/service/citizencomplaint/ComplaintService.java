package com.example.CivicConnect.service.citizencomplaint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.entity.geography.Department;
import com.example.CivicConnect.entity.geography.Ward;
import com.example.CivicConnect.entity.profiles.CitizenProfile;
import com.example.CivicConnect.entity.sla.ComplaintSla;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.ComplaintReportRepository;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintSlaRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;
import com.example.CivicConnect.repository.DepartmentRepository;
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


    //Register complaint
    public ComplaintResponseDTO registerComplaint(
            ComplaintRequestDTO request,
            User citizen) {

        CitizenProfile profile = citizenProfileRepository
                .findByUser_UserId(citizen.getUserId())
                .orElseThrow(() -> new RuntimeException("Citizen profile not found"));
        //block if ward not set
        if (profile.getWard() == null) {
            throw new RuntimeException(
                "Ward not set. Please update your profile before raising a complaint."
            );
        }
        
        Ward ward = profile.getWard();
        //fetching department
        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        //Duplicate check (24 hours)
        Optional<Complaint> duplicate =
                complaintRepository
                        .findByWard_WardIdAndDepartment_DepartmentIdAndTitleIgnoreCaseAndCreatedAtAfter(
                                ward.getWardId(),
                                department.getDepartmentId(),
                                request.getTitle(),
                                LocalDateTime.now().minusHours(24)
                        );

        if (duplicate.isPresent()) {
            Complaint existing = duplicate.get();
            //existing.setDuplicateCount(existing.getDuplicateCount() + 1);
            //Prevent same citizen reporting again
            if (complaintReportRepository.existsByComplaintAndCitizen_UserId(
                    existing,
                    citizen.getUserId())) {

                throw new RuntimeException(
                    "You have already reported this complaint."
                );
            }
            
            // Save citizen report
            ComplaintReport report = new ComplaintReport();
            report.setComplaint(existing);
            report.setCitizen(citizen);
            report.setDescription(request.getDescription());
            report.setLatitude(request.getLatitude());
            report.setLongitude(request.getLongitude());
            report.setReportedAt(LocalDateTime.now());
            complaintReportRepository.save(report);
            
            //Increment duplicate count
            existing.setDuplicateCount(existing.getDuplicateCount() + 1);
            complaintRepository.save(existing);

            //Notify citizen
            notificationService.notifyCitizen(
            	    citizen,
            	    "Duplicate Complaint",
            	    "A similar complaint already exists.",
            	    existing.getComplaintId(),
            	    NotificationType.COMPLAINT_CREATED
            	);


            return new ComplaintResponseDTO(
                    existing.getComplaintId(),
                    existing.getStatus().name(),
                    existing.getDuplicateCount(),
                    "Duplicate complaint"
            );
        }

        // Create new complaint
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
        complaint.setUpdatedAt(LocalDateTime.now());
        complaint.setCreatedBy(citizen);
        complaint.setLastUpdatedBy(citizen);

        complaintRepository.save(complaint);
        historyRepository.save(
                ComplaintStatusHistory.builder()
                        .complaint(complaint)
                        .status(ComplaintStatus.SUBMITTED)
                        .changedBy(citizen)
                        .systemGenerated(false)
                        .changedAt(LocalDateTime.now())
                        .remarks("Complaint submitted by citizen")
                        .build()
        );
        //CREATE FIRST REPORT (FOR NEW COMPLAINT)
        ComplaintReport report = new ComplaintReport();
        report.setComplaint(complaint);
        report.setCitizen(citizen);
        report.setDescription(request.getDescription());
        report.setLatitude(request.getLatitude());
        report.setLongitude(request.getLongitude());
        report.setReportedAt(LocalDateTime.now());
        complaintReportRepository.save(report);

        
        //CREATE SLA
        
        ComplaintSla sla = new ComplaintSla();
        sla.setComplaint(complaint);
        sla.setSlaStartTime(LocalDateTime.now());
        sla.setSlaDeadline(LocalDateTime.now().plusHours(department.getSlaHours()));
        sla.setStatus(SLAStatus.ACTIVE);
        sla.setEscalated(false);
        slaRepository.save(sla);
  

        // STATUS HISTORY
        logStatus(complaint, ComplaintStatus.SUBMITTED, citizen, false);

        // AUTO ASSIGN
        assignmentService.assignOfficer(complaint);

        // NOTIFY CITIZEN
        notificationService.notifyCitizen(
                citizen,
                "Complaint Registered",
                "Your complaint '" + complaint.getTitle() +
                        "' has been registered successfully (ID: " +
                        complaint.getComplaintId() + ")",
                complaint.getComplaintId(),
                NotificationType.COMPLAINT_CREATED
        );

        return new ComplaintResponseDTO(
                complaint.getComplaintId(),
                complaint.getStatus().name(),
                complaint.getDuplicateCount(),
                "Complaint registered successfully"
        );
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
    public ComplaintTrackingDTO trackComplaint(
            Long complaintId,
            Long citizenUserId) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (!complaint.getCitizen().getUserId().equals(citizenUserId)) {
            throw new RuntimeException("Access denied");
        }

        List<StatusHistoryDTO> history =
                historyRepository
                    .findByComplaint_ComplaintIdOrderByChangedAtAsc(complaintId)
                    .stream()
                    .map(h -> new StatusHistoryDTO(
                            h.getStatus(),
                            h.getChangedAt(),
                            h.getChangedBy() != null
                                    ? h.getChangedBy().getName()
                                    : "SYSTEM"
                    ))
                    .toList();
        String officerName = complaint.getAssignedOfficer() != null
                ? complaint.getAssignedOfficer().getName()
                : null;

        String officerMobile = complaint.getAssignedOfficer() != null
                ? complaint.getAssignedOfficer().getMobile()
                : null;


        List<String> imageUrls = complaint.getImages() != null 
                ? complaint.getImages().stream().map(img -> "/uploads/" + img.getImageUrl()).toList()
                : List.of();

        return new ComplaintTrackingDTO(
                complaint.getComplaintId(),
                complaint.getTitle(),
                complaint.getDescription(),
                complaint.getStatus(),
                officerName,
                officerMobile,
                history,
                imageUrls,
                complaint.getRating(),
                complaint.getFeedback()
        );

    }

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
                        c.getWard().getAreaName()
                ))
                .toList();
    }

    // SUBMIT FEEDBACK
    public void submitFeedback(Long complaintId, Long citizenUserId, Integer rating, String feedbackComments) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (!complaint.getCitizen().getUserId().equals(citizenUserId)) {
            throw new RuntimeException("Access denied: You can only provide feedback for your own complaints");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED && complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new RuntimeException("Feedback can only be provided for RESOLVED or CLOSED complaints");
        }

        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        complaint.setRating(rating);
        complaint.setFeedback(feedbackComments);
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
            sla.setStatus(com.example.CivicConnect.entity.enums.SLAStatus.ACTIVE);
            sla.setEscalated(false);
            slaRepository.save(sla);
        });

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
