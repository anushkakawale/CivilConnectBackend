package com.example.CivicConnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;

public interface ComplaintStatusHistoryRepository 
extends JpaRepository<ComplaintStatusHistory, Long>{
	List<ComplaintStatusHistory>
    findByComplaint_ComplaintIdOrderByChangedAtAsc(Long complaintId);
    
    List<ComplaintStatusHistory>
    findByComplaint_ComplaintIdOrderByChangedAtDesc(Long complaintId);

    List<ComplaintStatusHistory>
    findByComplaintOrderByChangedAtDesc(com.example.CivicConnect.entity.complaint.Complaint complaint);

    java.util.Optional<ComplaintStatusHistory> findFirstByComplaintAndStatusOrderByChangedAtDesc(
            com.example.CivicConnect.entity.complaint.Complaint complaint, 
            com.example.CivicConnect.entity.enums.ComplaintStatus status);
}
