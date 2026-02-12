package com.example.CivicConnect.controller.admincomplaint;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.AdminComplaintDTO;
import com.example.CivicConnect.dto.ComplaintDecisionDTO;
import com.example.CivicConnect.dto.ComplaintClosureDTO;
import com.example.CivicConnect.dto.PendingClosureTrackingDTO;
import com.example.CivicConnect.dto.ClosedComplaintTrackingDTO;
import com.example.CivicConnect.dto.ClosureApprovalQueueDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.admincomplaint.AdminComplaintService;
import com.example.CivicConnect.service.SharedComplaintService;

@RestController
@RequestMapping("/api/admin/complaints")
public class AdminComplaintController {

    private final AdminComplaintService service;
    private final SharedComplaintService sharedService;

    public AdminComplaintController(AdminComplaintService service, SharedComplaintService sharedService) {
        this.service = service;
        this.sharedService = sharedService;
    }

    @GetMapping("/{complaintId}")
    public ResponseEntity<?> getDetails(@PathVariable Long complaintId) {
        return ResponseEntity.ok(sharedService.getComplaintDetails(complaintId));
    }

    @PutMapping("/{complaintId}/close")
    public ResponseEntity<?> close(
            @PathVariable Long complaintId, 
            @RequestBody ComplaintDecisionDTO dto,
            Authentication authentication) {

        User admin = (User) authentication.getPrincipal();
        service.closeComplaint(complaintId, admin, dto.getRemarks());

        return ResponseEntity.ok("Complaint CLOSED successfully");
    }

    @GetMapping
    public ResponseEntity<?> allComplaints(Pageable pageable) {
        Page<Complaint> page = service.getAllComplaints(pageable);
        return mapToResponse(page);
    }

    @GetMapping("/unassigned")
    public ResponseEntity<?> unassigned(Pageable pageable) {
        Page<Complaint> page = service.getUnassignedComplaints(pageable);
        return mapToResponse(page);
    }

    @GetMapping("/pending-closure")
    public ResponseEntity<?> pendingClosure(Pageable pageable) {
        Page<Complaint> page = service.getPendingClosureComplaints(pageable);
        return mapToResponse(page);
    }

    @GetMapping("/pending-closure-queue")
    public ResponseEntity<?> pendingClosureQueue(Pageable pageable) {
        Page<ComplaintClosureDTO> page = service.getPendingClosureQueue(pageable);
        return ResponseEntity.ok(Map.of(
            "content", page.getContent(),
            "total", page.getTotalElements()
        ));
    }

    // 🆕 NEW: Detailed tracking for pending closures (like approval queue)
    @GetMapping("/pending-closure-tracking")
    public ResponseEntity<?> pendingClosureTracking(Pageable pageable) {
        Page<PendingClosureTrackingDTO> page = service.getPendingClosureTracking(pageable);
        return ResponseEntity.ok(Map.of(
            "content", page.getContent(),
            "totalPages", page.getTotalPages(),
            "totalElements", page.getTotalElements(),
            "currentPage", page.getNumber()
        ));
    }

    @GetMapping("/closed-history")
    public ResponseEntity<?> closedHistory(Pageable pageable) {
        Page<ComplaintClosureDTO> page = service.getClosedHistory(pageable);
        return ResponseEntity.ok(Map.of(
            "content", page.getContent(),
            "total", page.getTotalElements()
        ));
    }

    // 🆕 NEW: Detailed tracking for closed history (like approval queue)
    @GetMapping("/closed-tracking")
    public ResponseEntity<?> closedTracking(Pageable pageable) {
        Page<ClosedComplaintTrackingDTO> page = service.getClosedComplaintsTracking(pageable);
        return ResponseEntity.ok(Map.of(
            "content", page.getContent(),
            "totalPages", page.getTotalPages(),
            "totalElements", page.getTotalElements(),
            "currentPage", page.getNumber()
        ));
    }

    // 🆕 NEW: Closure Approval Queue (Similar to Ward Officer's Approval Queue)
    @GetMapping("/closure-approval-queue")
    public ResponseEntity<?> closureApprovalQueue(Pageable pageable) {
        Page<ClosureApprovalQueueDTO> page = service.getClosureApprovalQueue(pageable);
        return ResponseEntity.ok(Map.of(
            "content", page.getContent(),
            "totalPages", page.getTotalPages(),
            "totalElements", page.getTotalElements(),
            "currentPage", page.getNumber()
        ));
    }

    private ResponseEntity<?> mapToResponse(Page<Complaint> page) {
        List<AdminComplaintDTO> dtos = page.getContent().stream()
                .map(this::toAdminDto)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("data", dtos);
        response.put("total", page.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    private AdminComplaintDTO toAdminDto(Complaint c) {
        String priorityStr = (c.getPriority() != null) ? c.getPriority().name() : "MEDIUM";
        String slaStatusStr = (c.getSla() != null && c.getSla().getStatus() != null) 
            ? c.getSla().getStatus().name() : "ON_TRACK";
        String slaDeadlineStr = (c.getSla() != null && c.getSla().getSlaDeadline() != null) 
            ? c.getSla().getSlaDeadline().toString() : null;
            
        return new AdminComplaintDTO(
            c.getComplaintId(),
            c.getTitle(),
            c.getStatus() != null ? c.getStatus().name() : "SUBMITTED",
            c.getWard() != null ? c.getWard().getAreaName() : "Unknown",
            c.getDepartment() != null ? c.getDepartment().getName() : "Unknown",
            priorityStr,
            slaStatusStr,
            slaDeadlineStr,
            c.getCreatedAt() != null ? c.getCreatedAt().toString() : null
        );
    }
}
