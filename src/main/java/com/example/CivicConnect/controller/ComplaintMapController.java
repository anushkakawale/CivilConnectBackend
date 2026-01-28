package com.example.CivicConnect.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.ComplaintMapDTO;
import com.example.CivicConnect.service.citizencomplaint.ComplaintService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/complaints/map")
@RequiredArgsConstructor
public class ComplaintMapController {

    private final ComplaintService complaintService;

    @GetMapping
    public ResponseEntity<List<ComplaintMapDTO>> getMapComplaints() {
        return ResponseEntity.ok(
            complaintService.getComplaintsForMap()
        );
    }
}
