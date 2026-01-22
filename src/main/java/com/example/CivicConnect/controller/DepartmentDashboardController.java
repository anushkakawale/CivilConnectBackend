package com.example.CivicConnect.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.DepartmentDashboardService;

@RestController
@RequestMapping("/api/department/dashboard")
public class DepartmentDashboardController {

    private final DepartmentDashboardService service;

    public DepartmentDashboardController(
            DepartmentDashboardService service) {
        this.service = service;
    }

    @GetMapping("/assigned")
    public List<Complaint> assigned(Authentication auth) {
        User officer = (User) auth.getPrincipal();
        return service.myWork(officer.getUserId());
    }
}
