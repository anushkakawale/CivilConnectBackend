package com.example.CivicConnect.controller.wardcomplaint;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.wardcomplaint.WardOfficerComplaintService;

@RestController
@RequestMapping("/api/ward-officer/complaints")
public class WardOfficerComplaintController {

    private final WardOfficerComplaintService service;

    public WardOfficerComplaintController(WardOfficerComplaintService service) {
        this.service = service;
    }

    // ▶ APPROVE COMPLAINT (WARD OFFICER)
    @PutMapping("/{complaintId}/approve")
    public ResponseEntity<?> approveComplaint(
            @PathVariable Long complaintId,
            Authentication authentication) {

        User wardOfficer = (User) authentication.getPrincipal();
        service.approveComplaint(complaintId, wardOfficer);

        return ResponseEntity.ok("Complaint APPROVED by Ward Officer");
    }
}
