package com.example.CivicConnect.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.dto.ComplaintMapDTO;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapComplaintService {

    private final ComplaintRepository complaintRepository;
    private final CitizenProfileRepository citizenProfileRepository;
    private final OfficerProfileRepository officerProfileRepository;

    public List<ComplaintMapDTO> getMapComplaints(
            User user,
            ComplaintStatus status) {

        Long wardId = null;
        Long departmentId = null;

        switch (user.getRole()) {

            case CITIZEN -> {
                wardId = citizenProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow()
                        .getWard()
                        .getWardId();
            }

            case DEPARTMENT_OFFICER -> {
                OfficerProfile p = officerProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow();
                wardId = p.getWard().getWardId();
                departmentId = p.getDepartment().getDepartmentId();
            }

            case WARD_OFFICER -> {
                wardId = officerProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow()
                        .getWard()
                        .getWardId();
            }

            case ADMIN -> {
                // all null = no filter
            }
        }

        return complaintRepository
                .filterForMap(wardId, departmentId, status)
                .stream()
                .map(c -> {
                    ComplaintMapDTO dto = new ComplaintMapDTO();
                    dto.setComplaintId(c.getComplaintId());
                    dto.setLatitude(c.getLatitude() != null ? c.getLatitude() : 0.0);
                    dto.setLongitude(c.getLongitude() != null ? c.getLongitude() : 0.0);
                    dto.setStatus(c.getStatus());
                    dto.setSlaStatus(c.getSla() != null ? c.getSla().getStatus() : null);
                    dto.setTitle(c.getTitle());
                    dto.setDescription(c.getDescription());
                    dto.setDepartmentName(c.getDepartment() != null ? c.getDepartment().getName() : "N/A");
                    dto.setWardName(c.getWard() != null ? c.getWard().getAreaName() : "N/A");
                    
                    if (c.getImages() != null && !c.getImages().isEmpty()) {
                        dto.setImageUrl(c.getImages().get(0).getImageUrl());
                    }
                    
                    return dto;
                })
                .toList();
    }
    

}