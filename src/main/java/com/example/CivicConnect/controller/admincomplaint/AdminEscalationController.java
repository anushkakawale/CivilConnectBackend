package com.example.CivicConnect.controller.admincomplaint;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.repository.ComplaintSlaRepository;

@RestController
@RequestMapping("/api/admin/complaints")
public class AdminEscalationController {

	private final ComplaintSlaRepository slaRepository;

	public AdminEscalationController(
	        ComplaintSlaRepository slaRepository) {
	    this.slaRepository = slaRepository;
	}


	@GetMapping("/escalated")
	public ResponseEntity<?> escalatedComplaints() {
	    return ResponseEntity.ok(
	        slaRepository.findByStatus(SLAStatus.BREACHED)
	    );
	}

    
//    @GetMapping("/{complaintId}/sla")
//    public ResponseEntity<?> checkSla(@PathVariable Long complaintId) {
//
//        Complaint complaint = slaRepository.findById(complaintId)
//                .orElseThrow(() -> new RuntimeException("Complaint not found"));
//
//        return ResponseEntity.ok(
//            Map.of(
//                "complaintId", complaint.getComplaintId(),
//                "status", complaint.getStatus(),
//                "slaDeadline", complaint.getSlaDeadline(),
//                "escalated", complaint.isEscalated(),
//                "remainingMinutes",
//                Duration.between(
//                    LocalDateTime.now(),
//                    complaint.getSlaDeadline()
//                ).toMinutes()
//            )
//        );
//    }
}
