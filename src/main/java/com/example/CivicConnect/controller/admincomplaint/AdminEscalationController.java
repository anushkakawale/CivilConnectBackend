package com.example.CivicConnect.controller.admincomplaint;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.repository.ComplaintSlaRepository;

import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/admin/complaints")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEscalationController {

    private final ComplaintSlaRepository slaRepository;

    @GetMapping("/escalated")
    public ResponseEntity<?> escalatedComplaints() {

        return ResponseEntity.ok(
            slaRepository.findByStatus(SLAStatus.BREACHED)
                .stream()
                .map(sla -> Map.of(
                    "complaintId", sla.getComplaint().getComplaintId(),
                    "status", sla.getStatus(),
                    "deadline", sla.getSlaDeadline(),
                    "escalated", sla.isEscalated()
                ))
                .toList()
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
