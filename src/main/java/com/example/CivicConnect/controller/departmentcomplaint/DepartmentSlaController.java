package com.example.CivicConnect.controller.departmentcomplaint;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.OfficerProfileRepository;
import com.example.CivicConnect.service.SlaAnalyticsService;

@RestController
@RequestMapping("/api/department/analytics")
public class DepartmentSlaController {

    private final SlaAnalyticsService slaService;
    private final OfficerProfileRepository officerProfileRepository;

    public DepartmentSlaController(
            SlaAnalyticsService slaService,
            OfficerProfileRepository officerProfileRepository) {
        this.slaService = slaService;
        this.officerProfileRepository = officerProfileRepository;
    }

    // 👀 READ-ONLY SLA (Department Officer)
    @GetMapping("/sla")
    public ResponseEntity<?> departmentSla(Authentication auth) {

        User officer = (User) auth.getPrincipal();

        OfficerProfile profile =
                officerProfileRepository
                        .findByUser_UserId(officer.getUserId())
                        .orElseThrow(() ->
                                new RuntimeException("Officer profile not found"));

        return ResponseEntity.ok(
                slaService.departmentReport(
                        profile.getDepartment().getDepartmentId()
                )
        );
    }
}
