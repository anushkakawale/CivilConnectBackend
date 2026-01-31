package com.example.CivicConnect.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.ComplaintSummaryDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.DepartmentDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/department/dashboard")
@RequiredArgsConstructor
public class DepartmentDashboardController {

    private final DepartmentDashboardService service;

    @GetMapping("/summary")
    public com.example.CivicConnect.dto.DashboardSummaryDTO summary(Authentication auth) {
        User officer = (User) auth.getPrincipal();
        return service.getDashboardSummary(officer.getUserId());
    }

    @GetMapping("/assigned")
    public Page<ComplaintSummaryDTO> assigned(
            Pageable pageable,
            Authentication auth) {
        User officer = (User) auth.getPrincipal();
        return service.myWork(officer.getUserId(), pageable);
    }
}
