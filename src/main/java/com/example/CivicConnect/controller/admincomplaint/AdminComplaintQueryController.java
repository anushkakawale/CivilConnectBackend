package com.example.CivicConnect.controller.admincomplaint;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.repository.ComplaintRepository;

@RestController
@RequestMapping("/api/admin/complaints")
public class AdminComplaintQueryController {

    private final ComplaintRepository complaintRepository;

    public AdminComplaintQueryController(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    // ==============================
    // 2.1 ALL COMPLAINTS
    // ==============================
    @GetMapping
    public List<Complaint> allComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc();
    }

    // ==============================
    // 2.2 COMPLAINTS BY WARD
    // ==============================
    @GetMapping("/ward/{wardId}")
    public List<Complaint> byWard(@PathVariable Long wardId) {
        return complaintRepository.findByWard_WardId(wardId);
    }

    // ==============================
    // 2.3 COMPLAINTS BY DEPARTMENT
    // ==============================
    @GetMapping("/department/{deptId}")
    public List<Complaint> byDepartment(@PathVariable Long deptId) {
        return complaintRepository.findByDepartment_DepartmentId(deptId);
    }

    // ==============================
    // 2.4 COMPLAINTS BY STATUS
    // ==============================
    @GetMapping("/status/{status}")
    public List<Complaint> byStatus(@PathVariable ComplaintStatus status) {
        return complaintRepository.findByStatus(status);
    }
}
