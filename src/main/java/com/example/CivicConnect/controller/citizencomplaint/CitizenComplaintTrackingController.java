package com.example.CivicConnect.controller.citizencomplaint;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.service.citizencomplaint.ComplaintService;

@RestController
@RequestMapping("/api/citizen/complaints")
public class CitizenComplaintTrackingController {

    private final ComplaintService complaintService;

    public CitizenComplaintTrackingController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    //  View all complaints of citizen
    @GetMapping("/{citizenUserId}")
    public ResponseEntity<?> viewMyComplaints(
            @PathVariable Long citizenUserId) {

        return ResponseEntity.ok(
                complaintService.viewCitizenComplaints(citizenUserId)
        );
    }

    //  Track single complaint
    @GetMapping("/{citizenUserId}/{complaintId}")
    public ResponseEntity<?> trackComplaint(
            @PathVariable Long citizenUserId,
            @PathVariable Long complaintId) {

        return ResponseEntity.ok(
                complaintService.trackComplaint(complaintId, citizenUserId)
        );
    }
}
