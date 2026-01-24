package com.example.CivicConnect.controller.citizendashboard;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.citizendashboard.WardComplaintService;

@RestController
@RequestMapping("/api/ward/complaints")
public class WardComplaintController {

    private final WardComplaintService wardComplaintService;

    public WardComplaintController(WardComplaintService wardComplaintService) {
        this.wardComplaintService = wardComplaintService;
    }

    @GetMapping
    public ResponseEntity<?> viewWardComplaints(Authentication auth) {

        User officer = (User) auth.getPrincipal();

        return ResponseEntity.ok(
                wardComplaintService.getWardComplaints(officer)
        );
    }
}
