package com.example.CivicConnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.ComplaintStatus;

public interface ComplaintRepository
extends JpaRepository<Complaint, Long> {

Long countByCitizenUserId(Long userId);

Long countByCitizenUserIdAndStatus(Long userId, ComplaintStatus status);

List<Complaint> findByCitizenUserIdOrderByCreatedAtDesc(Long userId);

List<Complaint> findByWardId(Long wardId);
}
