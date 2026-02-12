package com.example.CivicConnect.service.admincomplaint;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.entity.sla.ComplaintSla;
import com.example.CivicConnect.entity.system.Notification;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintSlaRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;
import com.example.CivicConnect.repository.NotificationRepository;
import com.example.CivicConnect.repository.ComplaintApprovalRepository;
import com.example.CivicConnect.dto.ComplaintClosureDTO;
import com.example.CivicConnect.dto.ClosureApprovalQueueDTO;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
import com.example.CivicConnect.entity.enums.ImageStage;
import com.example.CivicConnect.service.NotificationService;
import com.example.CivicConnect.service.system.AccessLogService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final NotificationService notificationService;
    private final ComplaintSlaRepository slaRepository;
    private final AccessLogService accessLogService;
    private final ComplaintApprovalRepository approvalRepository;

    public AdminComplaintService(
            ComplaintRepository complaintRepository,
            ComplaintStatusHistoryRepository historyRepository,
            NotificationService notificationService,
            ComplaintSlaRepository slaRepository,
            AccessLogService accessLogService,
            ComplaintApprovalRepository approvalRepository) {

        this.complaintRepository = complaintRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
        this.slaRepository = slaRepository;
        this.accessLogService = accessLogService;
        this.approvalRepository = approvalRepository;
    }
 // ✅ PAGINATED LIST (NEW)
    public Page<Complaint> getAllComplaints(Pageable pageable) {
        return complaintRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    // ✅ NEW: UNASSIGNED COMPLAINTS
    public Page<Complaint> getUnassignedComplaints(Pageable pageable) {
        return complaintRepository.findByAssignedOfficerIsNull(pageable);
    }

    public Page<Complaint> getPendingClosureComplaints(Pageable pageable) {
        return complaintRepository.findByStatus(ComplaintStatus.APPROVED, pageable);
    }

    public Page<ComplaintClosureDTO> getPendingClosureQueue(Pageable pageable) {
        Page<Complaint> approved = complaintRepository.findByStatus(ComplaintStatus.APPROVED, pageable);
        return approved.map(this::mapToClosureDto);
    }

    // 🆕 NEW: Detailed tracking for pending closures (like approval queue)
    public Page<com.example.CivicConnect.dto.PendingClosureTrackingDTO> getPendingClosureTracking(Pageable pageable) {
        Page<Complaint> approved = complaintRepository.findByStatus(ComplaintStatus.APPROVED, pageable);
        return approved.map(this::mapToPendingClosureTracking);
    }

    private com.example.CivicConnect.dto.PendingClosureTrackingDTO mapToPendingClosureTracking(Complaint c) {
        String approvalRemarks = approvalRepository
                .findByComplaintAndStatus(c, ApprovalStatus.APPROVED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintApproval::getRemarks)
                .orElse("No remarks");

        // Get resolution remarks from history
        String resolutionRemarks = historyRepository
                .findFirstByComplaintAndStatusOrderByChangedAtDesc(c, ComplaintStatus.RESOLVED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintStatusHistory::getRemarks)
                .orElse("N/A");

        // Get resolved timestamp
        LocalDateTime resolvedAt = historyRepository
                .findFirstByComplaintAndStatusOrderByChangedAtDesc(c, ComplaintStatus.RESOLVED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintStatusHistory::getChangedAt)
                .orElse(null);

        // Calculate days waiting for closure
        Long daysWaiting = c.getApprovedAt() != null ? 
                java.time.Duration.between(c.getApprovedAt(), LocalDateTime.now()).toDays() : 0L;

        // Count images by stage
        long beforeCount = c.getImages() != null ? 
                c.getImages().stream()
                    .filter(img -> img.getImageStage() == com.example.CivicConnect.entity.enums.ImageStage.BEFORE_WORK)
                    .count() : 0;

        long afterCount = c.getImages() != null ? 
                c.getImages().stream()
                    .filter(img -> img.getImageStage() == com.example.CivicConnect.entity.enums.ImageStage.AFTER_RESOLUTION)
                    .count() : 0;

        return com.example.CivicConnect.dto.PendingClosureTrackingDTO.builder()
                .id(c.getComplaintId())
                .title(c.getTitle())
                .description(c.getDescription())
                .departmentName(c.getDepartment().getName())
                .wardName(c.getWard().getAreaName())
                .priority(c.getPriority().name())
                .citizenName(c.getCitizen().getName())
                .citizenMobile(c.getCitizen().getMobile())
                .assignedOfficerName(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getName() : "Unassigned")
                .assignedOfficerMobile(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getMobile() : "N/A")
                .approvedByName(c.getApprovedBy() != null ? c.getApprovedBy().getName() : "N/A")
                .status(c.getStatus().name())
                .slaStatus(c.getSla() != null ? c.getSla().getStatus().name() : "N/A")
                .slaBreached(c.isSlaBreached())
                .slaDeadline(c.getSla() != null ? c.getSla().getSlaDeadline() : null)
                .createdAt(c.getCreatedAt())
                .resolvedAt(resolvedAt)
                .approvedAt(c.getApprovedAt())
                .daysWaitingForClosure(daysWaiting)
                .resolutionRemarks(resolutionRemarks)
                .approvalRemarks(approvalRemarks)
                .beforeImageCount((int) beforeCount)
                .afterImageCount((int) afterCount)
                .hasResolutionImages(afterCount > 0)
                .build();
    }

    public Page<ComplaintClosureDTO> getClosedHistory(Pageable pageable) {
        Page<Complaint> closed = complaintRepository.findByStatus(ComplaintStatus.CLOSED, pageable);
        return closed.map(this::mapToClosureDto);
    }

    // 🆕 NEW: Detailed tracking for closed history (like approval queue)
    public Page<com.example.CivicConnect.dto.ClosedComplaintTrackingDTO> getClosedComplaintsTracking(Pageable pageable) {
        Page<Complaint> closed = complaintRepository.findByStatus(ComplaintStatus.CLOSED, pageable);
        return closed.map(this::mapToClosedTracking);
    }

    private com.example.CivicConnect.dto.ClosedComplaintTrackingDTO mapToClosedTracking(Complaint c) {
        String approvalRemarks = approvalRepository
                .findByComplaintAndStatus(c, ApprovalStatus.APPROVED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintApproval::getRemarks)
                .orElse("No remarks");

        String closureRemarks = historyRepository
                .findFirstByComplaintAndStatusOrderByChangedAtDesc(c, ComplaintStatus.CLOSED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintStatusHistory::getRemarks)
                .orElse("N/A");

        // Get resolved timestamp
        LocalDateTime resolvedAt = historyRepository
                .findFirstByComplaintAndStatusOrderByChangedAtDesc(c, ComplaintStatus.RESOLVED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintStatusHistory::getChangedAt)
                .orElse(null);

        // Count images by stage
        long beforeCount = c.getImages() != null ? 
                c.getImages().stream()
                    .filter(img -> img.getImageStage() == com.example.CivicConnect.entity.enums.ImageStage.BEFORE_WORK)
                    .count() : 0;

        long afterCount = c.getImages() != null ? 
                c.getImages().stream()
                    .filter(img -> img.getImageStage() == com.example.CivicConnect.entity.enums.ImageStage.AFTER_RESOLUTION)
                    .count() : 0;

        return com.example.CivicConnect.dto.ClosedComplaintTrackingDTO.builder()
                .id(c.getComplaintId())
                .title(c.getTitle())
                .description(c.getDescription())
                .departmentName(c.getDepartment().getName())
                .wardName(c.getWard().getAreaName())
                .priority(c.getPriority().name())
                .citizenName(c.getCitizen().getName())
                .assignedOfficerName(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getName() : "Unassigned")
                .approvedByName(c.getApprovedBy() != null ? c.getApprovedBy().getName() : "N/A")
                .closedByAdminName(c.getClosedByAdmin() != null ? c.getClosedByAdmin().getName() : "N/A")
                .status(c.getStatus().name())
                .slaStatus(c.getSla() != null ? c.getSla().getStatus().name() : "N/A")
                .slaBreached(c.isSlaBreached())
                .createdAt(c.getCreatedAt())
                .resolvedAt(resolvedAt)
                .approvedAt(c.getApprovedAt())
                .closedAt(c.getClosedAt())
                .approvalRemarks(approvalRemarks)
                .closureRemarks(closureRemarks)
                .averageRating(c.getAverageRating())
                .totalRatings(c.getTotalRatings())
                .beforeImageCount((int) beforeCount)
                .afterImageCount((int) afterCount)
                .build();
    }

    /**
     * 🆕 NEW: Closure Approval Queue (Similar to Ward Officer's Approval Queue)
     * Returns APPROVED complaints in a queue format for easy closure processing
     */
    public Page<ClosureApprovalQueueDTO> getClosureApprovalQueue(Pageable pageable) {
        Page<Complaint> approved = complaintRepository.findByStatus(ComplaintStatus.APPROVED, pageable);
        return approved.map(this::mapToClosureApprovalQueue);
    }

    private ClosureApprovalQueueDTO mapToClosureApprovalQueue(Complaint c) {
        String approvalRemarks = approvalRepository
                .findByComplaintAndStatus(c, ApprovalStatus.APPROVED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintApproval::getRemarks)
                .orElse("No remarks");

        String resolutionRemarks = historyRepository
                .findFirstByComplaintAndStatusOrderByChangedAtDesc(c, ComplaintStatus.RESOLVED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintStatusHistory::getRemarks)
                .orElse("N/A");

        LocalDateTime resolvedAt = historyRepository
                .findFirstByComplaintAndStatusOrderByChangedAtDesc(c, ComplaintStatus.RESOLVED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintStatusHistory::getChangedAt)
                .orElse(null);

        // Calculate waiting time
        Long daysWaiting = c.getApprovedAt() != null ? 
                java.time.Duration.between(c.getApprovedAt(), LocalDateTime.now()).toDays() : 0L;
        
        Long hoursWaiting = c.getApprovedAt() != null ? 
                java.time.Duration.between(c.getApprovedAt(), LocalDateTime.now()).toHours() : 0L;

        // Count images
        long beforeCount = c.getImages() != null ? 
                c.getImages().stream()
                    .filter(img -> img.getImageStage() == ImageStage.BEFORE_WORK)
                    .count() : 0;

        long afterCount = c.getImages() != null ? 
                c.getImages().stream()
                    .filter(img -> img.getImageStage() == ImageStage.AFTER_RESOLUTION)
                    .count() : 0;

        return ClosureApprovalQueueDTO.builder()
                .id(c.getComplaintId())
                .title(c.getTitle())
                .description(c.getDescription())
                .wardName(c.getWard().getAreaName())
                .departmentName(c.getDepartment().getName())
                .priority(c.getPriority().name())
                .citizenName(c.getCitizen().getName())
                .assignedOfficerName(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getName() : "Unassigned")
                .approvedBy(c.getApprovedBy() != null ? c.getApprovedBy().getName() : "N/A")
                .status(c.getStatus().name())
                .slaStatus(c.getSla() != null ? c.getSla().getStatus().name() : "N/A")
                .slaBreached(c.isSlaBreached())
                .slaDeadline(c.getSla() != null ? c.getSla().getSlaDeadline() : null)
                .createdAt(c.getCreatedAt())
                .resolvedAt(resolvedAt)
                .approvedAt(c.getApprovedAt())
                .daysWaitingForClosure(daysWaiting)
                .hoursWaitingForClosure(hoursWaiting)
                .approvalRemarks(approvalRemarks)
                .resolutionRemarks(resolutionRemarks)
                .beforeImageCount((int) beforeCount)
                .afterImageCount((int) afterCount)
                .hasResolutionImages(afterCount > 0)
                .averageRating(c.getAverageRating())
                .totalRatings(c.getTotalRatings())
                .build();
    }
    public void closeComplaint(Long complaintId, User admin, String remarks) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (complaint.getStatus() != ComplaintStatus.APPROVED) {
            throw new RuntimeException("Only APPROVED complaints can be CLOSED");
        }

        // =============================
        // 1️⃣ CLOSE COMPLAINT
        // =============================
        complaint.setStatus(ComplaintStatus.CLOSED);
        complaint.setClosedAt(LocalDateTime.now());
        complaint.setClosedByAdmin(admin);
        complaint.setLastUpdatedBy(admin);
        complaint.setUpdatedAt(LocalDateTime.now());

        // =============================
        // 2️⃣ CLOSE SLA (SAFE LOGIC)
        // =============================
        ComplaintSla sla = slaRepository
                .findByComplaint_ComplaintId(complaintId)
                .orElseThrow(() -> new RuntimeException("SLA not found"));

        sla.setSlaEndTime(LocalDateTime.now());

        if (sla.getStatus() != SLAStatus.BREACHED) {
            if (LocalDateTime.now().isAfter(sla.getSlaDeadline())) {
                sla.setStatus(SLAStatus.BREACHED);
                sla.setEscalated(true);
                complaint.setSlaBreached(true);
            } else {
                sla.setStatus(SLAStatus.MET);
                sla.setEscalated(false);
                sla.getSlaEndTime();
            }
        }

        // =============================
        // 3️⃣ STATUS HISTORY
        // =============================
        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setStatus(ComplaintStatus.CLOSED);
        history.setChangedBy(admin);
        history.setChangedAt(LocalDateTime.now());
        history.setRemarks(remarks != null ? remarks : "Complaint closed by Admin");

        // =============================
        // 4️⃣ SAVE CORE DATA FIRST
        // =============================
        complaintRepository.save(complaint);
        slaRepository.save(sla);
        historyRepository.save(history);

        // =============================
        // 5️⃣ NOTIFY (Pass Admin)
        // =============================
        notificationService.notifyComplaintClosed(complaint, admin);

        // =============================
        // 6️⃣ ACCESS LOG
        // =============================
        accessLogService.log(
            admin,
            "CLOSE_COMPLAINT",
            "COMPLAINT",
            complaintId,
            "SYSTEM"
        );
    }

    private com.example.CivicConnect.dto.ComplaintClosureDTO mapToClosureDto(Complaint c) {
        String approvalRemarks = approvalRepository
                .findByComplaintAndStatus(c, ApprovalStatus.APPROVED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintApproval::getRemarks)
                .orElse("No remarks");

        String closureRemarks = (c.getStatus() == ComplaintStatus.CLOSED) ? 
            historyRepository.findFirstByComplaintAndStatusOrderByChangedAtDesc(c, ComplaintStatus.CLOSED)
                .map(com.example.CivicConnect.entity.complaint.ComplaintStatusHistory::getRemarks)
                .orElse("N/A") : "N/A";

        return com.example.CivicConnect.dto.ComplaintClosureDTO.builder()
                .id(c.getComplaintId())
                .title(c.getTitle())
                .wardName(c.getWard().getAreaName())
                .departmentName(c.getDepartment().getName())
                .priority(c.getPriority().name())
                .assignedOfficerName(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getName() : "Unassigned")
                .approvedByOfficerName(c.getApprovedBy() != null ? c.getApprovedBy().getName() : "N/A")
                .closedByAdminName(c.getClosedByAdmin() != null ? c.getClosedByAdmin().getName() : "N/A")
                .status(c.getStatus().name())
                .slaStatus(c.getSla() != null ? c.getSla().getStatus().name() : "N/A")
                .createdAt(c.getCreatedAt())
                .approvedAt(c.getApprovedAt())
                .closedAt(c.getClosedAt())
                .approvalRemarks(approvalRemarks)
                .closureRemarks(closureRemarks)
                .build();
    }
}
