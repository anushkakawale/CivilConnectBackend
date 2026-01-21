package com.example.CivicConnect.controller.citizencomplaint;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.citizencomplaint.ComplaintService;

@RestController
@RequestMapping("/api/citizens/complaints")
public class CitizenComplaintTrackingController {

    private final ComplaintService complaintService;

    public CitizenComplaintTrackingController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    //  View all complaints of citizen
    @GetMapping
    public ResponseEntity<?> viewMyComplaints(
    		Authentication auth) {
    	User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                complaintService.viewCitizenComplaints(citizen.getUserId())
        );
    }

    //  Track single complaint
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