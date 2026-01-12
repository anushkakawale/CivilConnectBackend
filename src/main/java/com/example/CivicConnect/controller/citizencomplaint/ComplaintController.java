package com.example.CivicConnect.controller.citizencomplaint;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.ComplaintRequestDTO;
import com.example.CivicConnect.dto.ComplaintResponseDTO;
import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.service.citizencomplaint.ComplaintService;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    // ✅ REGISTER COMPLAINT
    @PostMapping
    public ResponseEntity<ComplaintResponseDTO> registerComplaint(
            @RequestBody ComplaintRequestDTO request) {

        return ResponseEntity.ok(
                complaintService.registerComplaint(request)
        );
    }

    // ✅ TRACK COMPLAINTS
    @GetMapping("/citizen/{citizenUserId}")
    public ResponseEntity<List<Complaint>> getCitizenComplaints(
            @PathVariable Long citizenUserId) {

        return ResponseEntity.ok(
                complaintService.getCitizenComplaints(citizenUserId)
        );
    }
}
