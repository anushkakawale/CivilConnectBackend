package com.example.CivicConnect.controller.citizencomplaint;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.sla.ComplaintSla;
import com.example.CivicConnect.repository.ComplaintSlaRepository;

@RestController
@RequestMapping("/api/citizens/complaints")
public class CitizenSlaController {

    private final ComplaintSlaRepository slaRepository;

    public CitizenSlaController(ComplaintSlaRepository slaRepository) {
        this.slaRepository = slaRepository;
    }

    // 🔍 View SLA details (existing)
    @GetMapping("/{complaintId}/sla")
    public ResponseEntity<?> mySla(
            @PathVariable Long complaintId,
            Authentication auth) {

        User citizen = (User) auth.getPrincipal();

        return ResponseEntity.ok(
            slaRepository
                .findByComplaint_ComplaintIdAndComplaint_Citizen_UserId(
                    complaintId,
                    citizen.getUserId()
                )
                .orElseThrow(() -> new RuntimeException("SLA not found"))
        );
    }

    // ⏱ SLA COUNTDOWN (NEW)
    @GetMapping("/{complaintId}/sla/countdown")
    public ResponseEntity<?> slaCountdown(
            @PathVariable Long complaintId,
            Authentication auth) {

        User citizen = (User) auth.getPrincipal();

        ComplaintSla sla = slaRepository
                .findByComplaint_ComplaintIdAndComplaint_Citizen_UserId(
                        complaintId,
                        citizen.getUserId()
                )
                .orElseThrow(() -> new RuntimeException("SLA not found"));

        long remainingMinutes =
                Duration.between(LocalDateTime.now(), sla.getSlaDeadline())
                        .toMinutes();

        return ResponseEntity.ok(Map.of(
                "deadline", sla.getSlaDeadline(),
                "remainingMinutes", remainingMinutes,
                "breached", remainingMinutes < 0
        ));
    }
}