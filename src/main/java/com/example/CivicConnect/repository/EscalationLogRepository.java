package com.example.CivicConnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.complaint.EscalationLog;

public interface EscalationLogRepository extends JpaRepository<EscalationLog, Long> {
}