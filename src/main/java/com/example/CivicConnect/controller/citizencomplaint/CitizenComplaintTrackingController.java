package com.example.CivicConnect.controller.citizencomplaint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.ComplaintSummaryDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.service.citizencomplaint.ComplaintService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/citizens/complaints")
@RequiredArgsConstructor
public class CitizenComplaintTrackingController {

    private final ComplaintService complaintService;
    private final ComplaintRepository complaintRepository;

    // ✅ View all complaints of citizen (PAGINATED)
    @GetMapping
    public Page<ComplaintSummaryDTO> viewMyComplaints(
            Pageable pageable,
            Authentication auth) {
        User citizen = (User) auth.getPrincipal();
        
        return complaintRepository
                .findByCitizen_UserIdOrderByCreatedAtDesc(citizen.getUserId(), pageable)
                .map(c -> new ComplaintSummaryDTO(
                        c.getComplaintId(),
                        c.getTitle(),
                        c.getStatus(),
                        c.getCreatedAt()
                ));
    }

    // ✅ Track single complaint
    @GetMapping("/{complaintId}")
    public ResponseEntity<?> trackComplaint(
            @PathVariable Long complaintId,
            Authentication auth) {

        User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                complaintService.trackComplaint(complaintId, citizen.getUserId())
        );
    }
}
