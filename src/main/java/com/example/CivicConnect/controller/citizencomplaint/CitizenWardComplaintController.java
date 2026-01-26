package com.example.CivicConnect.controller.citizencomplaint;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.profiles.CitizenProfile;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.ComplaintRepository;

@RestController
@RequestMapping("/api/citizens/ward-complaints")
public class CitizenWardComplaintController {

    private final CitizenProfileRepository profileRepo;
    private final ComplaintRepository complaintRepository;

    public CitizenWardComplaintController(
        CitizenProfileRepository profileRepo,
        ComplaintRepository complaintRepository) {
        this.profileRepo = profileRepo;
        this.complaintRepository = complaintRepository;
    }

    @GetMapping
    public ResponseEntity<?> wardComplaints(
            @RequestParam(required = false) Long departmentId,
            Authentication auth) {
        User citizen = (User) auth.getPrincipal();

        CitizenProfile profile = profileRepo
            .findByUser_UserId(citizen.getUserId())
            .orElseThrow();

        if (profile.getWard() == null) {
            return ResponseEntity.ok(List.of());
        }

        List<Complaint> complaints;
        if (departmentId != null) {
            complaints = complaintRepository
                .findByWard_WardIdAndDepartment_DepartmentIdOrderByCreatedAtDesc(
                    profile.getWard().getWardId(), departmentId);
        } else {
            complaints = complaintRepository
                .findByWard_WardIdOrderByCreatedAtDesc(
                    profile.getWard().getWardId());
        }

        return ResponseEntity.ok(complaints);
    }
}
