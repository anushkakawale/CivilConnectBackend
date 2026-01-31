package com.example.CivicConnect.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class DashboardCountService {

    private final ComplaintRepository complaintRepository;
    private final CitizenProfileRepository citizenProfileRepository;
    private final OfficerProfileRepository officerProfileRepository;

    public Map<String, Long> getDashboardCounts(User user) {

        Map<String, Long> counts = new HashMap<>();

        for (ComplaintStatus status : ComplaintStatus.values()) {
            counts.put(status.name(), countByRole(user, status));
        }

        return counts;
    }

    private long countByRole(User user, ComplaintStatus status) {

        return switch (user.getRole()) {

            case CITIZEN -> {
                Long wardId = citizenProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow()
                        .getWard()
                        .getWardId();

                yield complaintRepository
                        .countByWard_WardIdAndStatus(wardId, status);
            }

            case DEPARTMENT_OFFICER -> {
                OfficerProfile p = officerProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow();

                yield complaintRepository
                        .countByWard_WardIdAndDepartment_DepartmentIdAndStatus(
                                p.getWard().getWardId(),
                                p.getDepartment().getDepartmentId(),
                                status
                        );
            }

            case WARD_OFFICER -> {
                Long wardId = officerProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow()
                        .getWard()
                        .getWardId();

                yield complaintRepository
                        .countByWard_WardIdAndStatus(wardId, status);
            }

            case ADMIN -> complaintRepository.countByStatus(status);
        };
    }
}
