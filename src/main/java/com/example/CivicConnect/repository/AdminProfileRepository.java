package com.example.CivicConnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.profiles.AdminProfile;

public interface AdminProfileRepository
        extends JpaRepository<AdminProfile, Long> {

    boolean existsByUser(User user);
    //List<Complaint> findByStatus(ComplaintStatus status);

}
