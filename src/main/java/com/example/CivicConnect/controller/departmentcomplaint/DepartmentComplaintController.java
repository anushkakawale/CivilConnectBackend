package com.example.CivicConnect.controller.departmentcomplaint;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.service.departmentcomplaint.DepartmentComplaintService;

@RestController
@RequestMapping("/api/department/complaints")
public class DepartmentComplaintController {

    private final DepartmentComplaintService service;

    public DepartmentComplaintController(DepartmentComplaintService service) {
        this.service = service;
    }

    @PutMapping("/{complaintId}/start/{officerUserId}")
    public ResponseEntity<?> start(
            @PathVariable Long complaintId,
            @PathVariable Long officerUserId) {

        service.startWork(complaintId, officerUserId);
        return ResponseEntity.ok("Complaint marked IN_PROGRESS");
    }

    @PutMapping("/{complaintId}/resolve/{officerUserId}")
    public ResponseEntity<?> resolve(
            @PathVariable Long complaintId,
            @PathVariable Long officerUserId) {

        service.resolve(complaintId, officerUserId);
        return ResponseEntity.ok("Complaint marked RESOLVED");
    }
}
