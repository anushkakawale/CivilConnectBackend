package com.example.CivicConnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.complaint.ComplaintUpdate;

public interface ComplaintUpdateRepository
extends JpaRepository<ComplaintUpdate, Long> {

List<ComplaintUpdate> findByComplaint_ComplaintIdOrderByUpdatedAtAsc(Long complaintId);
}
