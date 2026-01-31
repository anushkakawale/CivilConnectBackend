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
                .map(c -> new ComplaintMapDTO(
                	    c.getComplaintId(),
                	    c.getLatitude(),
                	    c.getLongitude(),
                	    c.getStatus(),
                	    c.getSla() != null ? c.getSla().getStatus() : null
                	))

                .toList();
    }
    

}