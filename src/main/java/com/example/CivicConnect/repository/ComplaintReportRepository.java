package com.example.CivicConnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintReport;

public interface ComplaintReportRepository extends JpaRepository<ComplaintReport, Long> {

	List<ComplaintReport> findByComplaint(Complaint complaint);

	boolean existsByComplaintAndCitizen_UserId(Complaint complaint, Long citizenUserId);
}