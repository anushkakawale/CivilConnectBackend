package com.example.CivicConnect.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.service.GlobalSearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class GlobalSearchController {

    private final GlobalSearchService searchService;

    @GetMapping("/complaints")
    public ResponseEntity<?> searchComplaints(
            @RequestParam String query,
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) Long departmentId,
            Pageable pageable,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        
        // Role-based search scope
        Page<?> results;
        if (user.getRole() == RoleName.ADMIN) {
            results = searchService.searchAllComplaints(query, wardId, departmentId, pageable);
        } else if (user.getRole() == RoleName.WARD_OFFICER) {
            results = searchService.searchWardComplaints(query, user.getUserId(), departmentId, pageable);
        } else if (user.getRole() == RoleName.DEPARTMENT_OFFICER) {
            results = searchService.searchDepartmentComplaints(query, user.getUserId(), pageable);
        } else {
            // Citizens can only search their own complaints
            results = searchService.searchCitizenComplaints(query, user.getUserId(), pageable);
        }

        return ResponseEntity.ok(results);
    }
}
