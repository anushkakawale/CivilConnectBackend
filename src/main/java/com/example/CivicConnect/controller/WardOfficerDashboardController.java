package com.example.CivicConnect.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.complaint.ComplaintApproval;
import com.example.CivicConnect.service.WardOfficerDashboardService;

@RestController
@RequestMapping("/api/ward-officer/dashboard")
public class WardOfficerDashboardController {

    private final WardOfficerDashboardService service;

    public WardOfficerDashboardController(
            WardOfficerDashboardService service) {
        this.service = service;
    }

    @GetMapping("/pending-approvals")
    public List<ComplaintApproval> pendingApprovals() {
        return service.getPendingApprovals();
    }
}
