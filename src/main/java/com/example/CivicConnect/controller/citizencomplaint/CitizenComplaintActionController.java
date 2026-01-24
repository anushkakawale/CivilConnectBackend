package com.example.CivicConnect.controller.citizencomplaint;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.citizencomplaint.CitizenComplaintActionService;

//when citizen reopens his complaint again 
//works only when status is RESOLVED or CLOSED amd only within 7 days complaint can be REOPENED
//sla starts again
@RestController
@RequestMapping("/api/citizens/complaints")
public class CitizenComplaintActionController {

    private final CitizenComplaintActionService service;

    public CitizenComplaintActionController(CitizenComplaintActionService service) {
        this.service = service;
    }

    @PutMapping("/{complaintId}/reopen")
    public ResponseEntity<?> reopenComplaint(
            @PathVariable Long complaintId,
            Authentication auth) {

        User citizen = (User) auth.getPrincipal();
        service.reopenComplaint(complaintId, citizen.getUserId());

        return ResponseEntity.ok("Complaint reopened successfully");
    }
}
