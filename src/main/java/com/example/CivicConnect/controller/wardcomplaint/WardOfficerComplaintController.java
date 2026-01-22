package com.example.CivicConnect.controller.wardcomplaint;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.ComplaintDecisionDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.wardcomplaint.WardOfficerComplaintService;

@RestController
@RequestMapping("/api/ward-officer/complaints")
public class WardOfficerComplaintController {

    private final WardOfficerComplaintService service;

    public WardOfficerComplaintController(WardOfficerComplaintService service) {
        this.service = service;
    }

 // ✅ APPROVE COMPLAINT
    @PutMapping("/{complaintId}/approve")
    public ResponseEntity<?> approve(
            @PathVariable Long complaintId,
            @RequestBody ComplaintDecisionDTO dto,
            Authentication authentication) {

        User officer = (User) authentication.getPrincipal();
        service.approve(complaintId, officer, dto.getRemarks());
        return ResponseEntity.ok("Complaint APPROVED");
    }

    @PutMapping("/{complaintId}/reject")
    public ResponseEntity<?> reject(
            @PathVariable Long complaintId,
            @RequestBody ComplaintDecisionDTO dto,
            Authentication authentication) {

        User officer = (User) authentication.getPrincipal();
        service.reject(complaintId, officer, dto.getRemarks());
        return ResponseEntity.ok("Complaint REJECTED");
    }


}
