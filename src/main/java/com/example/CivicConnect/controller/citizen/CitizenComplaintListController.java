package com.example.CivicConnect.controller.citizen;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.Priority;
import com.example.CivicConnect.service.citizen.CitizenComplaintListService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/citizen")
@RequiredArgsConstructor
public class CitizenComplaintListController {

    private final CitizenComplaintListService complaintListService;

    @GetMapping("/my-complaints")
    public ResponseEntity<Map<String, Object>> getMyComplaints(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) Priority priority
    ) {
        User citizen = (User) auth.getPrincipal();
        Map<String, Object> complaints = complaintListService.getMyComplaints(
            citizen, page, size, status, priority,null
        );
        return ResponseEntity.ok(complaints);
    }
}
