package com.example.CivicConnect.controller.admincomplaint;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.admincomplaint.AdminComplaintService;

@RestController
@RequestMapping("/api/admin/complaints")
public class AdminComplaintController {

    private final AdminComplaintService service;

    public AdminComplaintController(AdminComplaintService service) {
        this.service = service;
    }

    //  CLOSE COMPLAINT
    @PutMapping("/{complaintId}/close")
    public ResponseEntity<?> close(
            @PathVariable Long complaintId,
            Authentication authentication) {

        User admin = (User) authentication.getPrincipal();
        service.closeComplaint(complaintId, admin);

        return ResponseEntity.ok("Complaint CLOSED successfully");
    }


}