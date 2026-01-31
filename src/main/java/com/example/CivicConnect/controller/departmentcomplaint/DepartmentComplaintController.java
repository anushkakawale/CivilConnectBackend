package com.example.CivicConnect.controller.departmentcomplaint;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.service.departmentcomplaint.DepartmentComplaintService;

@RestController
@RequestMapping("/api/department/complaints")
public class DepartmentComplaintController {

    private final DepartmentComplaintService service;

    public DepartmentComplaintController(DepartmentComplaintService service) {
        this.service = service;
    }

    // ▶ START WORK (DEPARTMENT OFFICER)
    @PutMapping("/{complaintId}/start")
    public ResponseEntity<?> start(
            @PathVariable Long complaintId,
            Authentication authentication) {

        User officer = (User) authentication.getPrincipal();
        service.startWork(complaintId, officer);

        return ResponseEntity.ok("Complaint marked IN_PROGRESS");
    }

    // ▶ RESOLVE WORK (DEPARTMENT OFFICER)
    @PutMapping("/{complaintId}/resolve")
    public ResponseEntity<?> resolve(
            @PathVariable Long complaintId,
            Authentication authentication) {

        User officer = (User) authentication.getPrincipal();
        service.resolve(complaintId, officer);

        return ResponseEntity.ok("Complaint marked RESOLVED");
    }

}
