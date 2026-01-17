package com.example.CivicConnect.controller.admincomplaint;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.service.admincomplaint.AdminComplaintService;

@RestController
@RequestMapping("/api/admin/complaints")
public class AdminComplaintController {

    private final AdminComplaintService service;

    public AdminComplaintController(AdminComplaintService service) {
        this.service = service;
    }

    //  CLOSE COMPLAINT
    @PutMapping("/{complaintId}/close/{adminUserId}")
    public ResponseEntity<?> closeComplaint(
            @PathVariable Long complaintId,
            @PathVariable Long adminUserId) {

        service.closeComplaint(complaintId, adminUserId);
        return ResponseEntity.ok("Complaint CLOSED successfully");
    }
}