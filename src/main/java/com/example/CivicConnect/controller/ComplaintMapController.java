package com.example.CivicConnect.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.ComplaintMapDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.service.MapComplaintService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/complaints/map")
@RequiredArgsConstructor
public class ComplaintMapController {

    private final MapComplaintService mapService;

    @GetMapping
    public ResponseEntity<List<ComplaintMapDTO>> getMapData(
            @RequestParam(required = false) ComplaintStatus status,
            Authentication auth) {

        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(
                mapService.getMapComplaints(user, status)
        );
    }
}
