package com.example.CivicConnect.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.SlaCountdownDTO;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.entity.sla.ComplaintSla;
import com.example.CivicConnect.repository.ComplaintSlaRepository;

@Service
public class SlaCountdownService {

    private final ComplaintSlaRepository slaRepository;

    public SlaCountdownService(ComplaintSlaRepository slaRepository) {
        this.slaRepository = slaRepository;
    }

    public SlaCountdownDTO getCountdown(Long complaintId) {

        ComplaintSla sla = slaRepository
                .findByComplaint_ComplaintId(complaintId)
                .orElseThrow(() ->
                        new RuntimeException("SLA not found for complaint"));

        LocalDateTime now = LocalDateTime.now();

        long remainingMinutes =
                Duration.between(now, sla.getSlaDeadline()).toMinutes();

        return new SlaCountdownDTO(
                sla.getSlaDeadline(),
                Math.max(remainingMinutes, 0),
                sla.getStatus() == SLAStatus.BREACHED
        );
    }
}