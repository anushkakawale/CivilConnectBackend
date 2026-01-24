package com.example.CivicConnect.controller.admincomplaint;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.sla.ComplaintSla;
import com.example.CivicConnect.repository.ComplaintSlaRepository;

@RestController
@RequestMapping("/api/admin/sla")
public class AdminSlaController {

    private final ComplaintSlaRepository slaRepository;

    public AdminSlaController(ComplaintSlaRepository slaRepository) {
        this.slaRepository = slaRepository;
    }

    // ✅ Full SLA details
    @GetMapping("/{complaintId}")
    public ResponseEntity<?> checkSla(@PathVariable Long complaintId) {

        ComplaintSla sla =
                slaRepository.findByComplaint_ComplaintId(complaintId)
                        .orElseThrow(() ->
                                new RuntimeException("SLA not found"));

        return ResponseEntity.ok(
                Map.of(
                        "complaintId", complaintId,
                        "status", sla.getStatus(),
                        "slaStart", sla.getSlaStartTime(),
                        "slaDeadline", sla.getSlaDeadline(),
                        "escalated", sla.isEscalated()
                )
        );
    }

    // ✅ Remaining time API (ADD THIS)
    @GetMapping("/{complaintId}/remaining")
    public ResponseEntity<?> remainingTime(@PathVariable Long complaintId) {

        ComplaintSla sla =
                slaRepository.findByComplaint_ComplaintId(complaintId)
                        .orElseThrow(() ->
                                new RuntimeException("SLA not found"));

        long remainingMinutes =
                Duration.between(
                        LocalDateTime.now(),
                        sla.getSlaDeadline()
                ).toMinutes();

        return ResponseEntity.ok(
                Map.of(
                        "complaintId", complaintId,
                        "remainingMinutes", remainingMinutes
                )
        );
    }
}
