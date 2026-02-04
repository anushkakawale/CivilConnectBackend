package com.example.CivicConnect.controller.admincomplaint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.admincomplaint.AdminComplaintService;

@RestController
@RequestMapping("/api/admin/complaints")
public class AdminComplaintController {

	private final AdminComplaintService service;
	private final com.example.CivicConnect.service.SharedComplaintService sharedService;

	public AdminComplaintController(AdminComplaintService service, com.example.CivicConnect.service.SharedComplaintService sharedService) {
		this.service = service;
		this.sharedService = sharedService;
	}

	@GetMapping("/{complaintId}")
	public ResponseEntity<?> getDetails(@PathVariable Long complaintId) {
		return ResponseEntity.ok(sharedService.getComplaintDetails(complaintId));
	}


	// CLOSE COMPLAINT
	@PutMapping("/{complaintId}/close")
	public ResponseEntity<?> close(@PathVariable Long complaintId, Authentication authentication) {

		User admin = (User) authentication.getPrincipal();
		service.closeComplaint(complaintId, admin);

		return ResponseEntity.ok("Complaint CLOSED successfully");
	}

	@GetMapping
	public ResponseEntity<?> allComplaints(Pageable pageable) {
		Page<Complaint> page = service.getAllComplaints(pageable);
		return ResponseEntity.ok(java.util.Map.of(
			"data", page.getContent().stream()
				.map(c -> new com.example.CivicConnect.dto.AdminComplaintDTO(
					c.getComplaintId(),
					c.getTitle(),
					c.getStatus().name(),
					c.getWard().getAreaName(),
					c.getDepartment().getName(),
					c.getCreatedAt().toString()
				)).toList(),
			"total", page.getTotalElements()
		));
	}

    // ✅ PENDING CLOSURE QUEUE
    @GetMapping("/pending-closure")
    public ResponseEntity<?> pendingClosure(Pageable pageable) {
        Page<Complaint> page = service.getPendingClosureComplaints(pageable);
        return ResponseEntity.ok(java.util.Map.of(
            "data", page.getContent().stream()
                .map(c -> new com.example.CivicConnect.dto.AdminComplaintDTO(
                    c.getComplaintId(),
                    c.getTitle(),
                    c.getStatus().name(),
                    c.getWard().getAreaName(),
                    c.getDepartment().getName(),
                    c.getCreatedAt().toString()
                )).toList(),
            "total", page.getTotalElements()
        ));
    }

}
