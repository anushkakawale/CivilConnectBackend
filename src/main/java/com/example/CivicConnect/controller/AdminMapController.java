package com.example.CivicConnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.service.map.ComplaintMapService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/map")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMapController {

    private final ComplaintMapService mapService;

    @GetMapping("/city")
    public ResponseEntity<?> cityMap() {
        return ResponseEntity.ok(mapService.cityMap());
    }

    @GetMapping("/ward/{wardId}")
    public ResponseEntity<?> wardMap(@PathVariable Long wardId) {
        return ResponseEntity.ok(mapService.wardMap(wardId));
    }
}
