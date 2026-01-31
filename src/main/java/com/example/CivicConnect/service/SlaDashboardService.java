package com.example.CivicConnect.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.CitizenProfileRepository;
import com.example.CivicConnect.repository.ComplaintSlaRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlaDashboardService {

    private final ComplaintSlaRepository slaRepository;
    private final CitizenProfileRepository citizenProfileRepository;
    private final OfficerProfileRepository officerProfileRepository;

    public Map<String, Long> getSlaStats(User user) {

        Map<String, Long> stats = new HashMap<>();

        for (SLAStatus status : SLAStatus.values()) {
            stats.put(status.name(), countByRole(user, status));
        }
        return stats;
    }

    private long countByRole(User user, SLAStatus status) {

        return switch (user.getRole()) {

            case CITIZEN -> {
                Long wardId = citizenProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow()
                        .getWard()
                        .getWardId();

                yield slaRepository
                        .countByComplaint_Ward_WardIdAndStatus(wardId, status);
            }

            case DEPARTMENT_OFFICER -> {
                OfficerProfile p = officerProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow();

                yield slaRepository
                        .countByComplaint_Ward_WardIdAndComplaint_Department_DepartmentIdAndStatus(
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

                yield slaRepository
                        .countByComplaint_Ward_WardIdAndStatus(wardId, status);
            }

            case ADMIN -> slaRepository.countByStatus(status);
        };
    }
}
