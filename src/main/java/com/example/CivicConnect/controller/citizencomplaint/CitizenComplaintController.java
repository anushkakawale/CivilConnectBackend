package com.example.CivicConnect.controller.citizencomplaint;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.Priority;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.service.citizen.CitizenComplaintListService;

import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/citizen/complaints")
@RequiredArgsConstructor
public class CitizenComplaintController {

    private final CitizenComplaintListService citizenComplaintListService;

    @GetMapping("/{id}/timeline")
    public ResponseEntity<?> timeline(@PathVariable Long id) {
        return ResponseEntity.ok(citizenComplaintListService.getTimeline(id));
    }

    @GetMapping
    public ResponseEntity<?> complaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) SLAStatus slaStatus,
            Authentication auth
    ) {

        User citizen = (User) auth.getPrincipal();

        return ResponseEntity.ok(
                citizenComplaintListService.getMyComplaints(
                        citizen,
                        page,
                        size,
                        status,
                        priority,
                        slaStatus
                )
        );
    }
}
