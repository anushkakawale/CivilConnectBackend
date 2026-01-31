package com.example.CivicConnect.controller.citizen;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.citizen.CitizenAreaComplaintsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/citizen")
@RequiredArgsConstructor
public class CitizenAreaComplaintsController {

    private final CitizenAreaComplaintsService areaComplaintsService;

    @GetMapping("/area-complaints")
    public ResponseEntity<List<Map<String, Object>>> getAreaComplaints(
            Authentication auth,
            @RequestParam(required = false) Long wardId
    ) {
        User citizen = (User) auth.getPrincipal();
        List<Map<String, Object>> complaints = areaComplaintsService.getAreaComplaints(citizen, wardId);
        return ResponseEntity.ok(complaints);
    }
}
