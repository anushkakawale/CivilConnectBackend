package com.example.CivicConnect.controller.citizencomplaint;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.service.citizencomplaint.CitizenComplaintQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/citizen/complaints")
@RequiredArgsConstructor
public class CitizenComplaintController {

    private final CitizenComplaintQueryService queryService;

    @GetMapping("/{id}/timeline")
    public ResponseEntity<?> timeline(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.getTimeline(id));
    }
}
