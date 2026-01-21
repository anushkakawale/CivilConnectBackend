package com.example.CivicConnect.controller.citizencomplaint;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.ComplaintRequestDTO;
import com.example.CivicConnect.dto.ComplaintResponseDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.citizencomplaint.ComplaintService;

@RestController
@RequestMapping("/api/citizens/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    //  REGISTER COMPLAINT
    @PostMapping
    public ResponseEntity<ComplaintResponseDTO> registerComplaint(
            @RequestBody ComplaintRequestDTO request, Authentication auth) {
    	User citizen = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                complaintService.registerComplaint(request, citizen)
        );
    }

//    //  TRACK COMPLAINTS
//    @GetMapping("/citizen/{citizenUserId}")
//    public ResponseEntity<List<Complaint>> getCitizenComplaints(
//            @PathVariable Long citizenUserId) {
//
//        return ResponseEntity.ok(
//                complaintService.getCitizenComplaints(citizenUserId)
//        );
//    }
}
