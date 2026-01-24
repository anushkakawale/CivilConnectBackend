package com.example.CivicConnect.service.citizendashboard;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.WardComplaintDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

@Service
public class WardComplaintService {

    private final ComplaintRepository complaintRepository;
    private final OfficerProfileRepository officerProfileRepository;

    public WardComplaintService(
            ComplaintRepository complaintRepository,
            OfficerProfileRepository officerProfileRepository) {
        this.complaintRepository = complaintRepository;
        this.officerProfileRepository = officerProfileRepository;
    }

    public List<WardComplaintDTO> getWardComplaints(User officerUser) {

        OfficerProfile officerProfile =
                officerProfileRepository
                        .findByUser_UserId(officerUser.getUserId())
                        .orElseThrow(() ->
                                new RuntimeException("Officer profile not found"));

        Long wardId = officerProfile.getWard().getWardId();

        return complaintRepository
                .findByWard_WardIdOrderByCreatedAtDesc(wardId)
                .stream()
                .map(c -> new WardComplaintDTO(
                        c.getComplaintId(),
                        c.getTitle(),
                        c.getCitizen().getName(),
                        c.getDepartment().getName(),
                        c.getStatus(),
                        c.getCreatedAt(),
                        c.isEscalated(),
                        c.isSlaBreached()
                ))
                .toList();
    }
}
