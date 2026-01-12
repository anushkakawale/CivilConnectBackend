package com.example.CivicConnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;

public interface ComplaintStatusHistoryRepository 
extends JpaRepository<ComplaintStatusHistory, Long>{

}
