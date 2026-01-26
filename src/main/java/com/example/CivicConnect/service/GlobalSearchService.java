package com.example.CivicConnect.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.CivicConnect.dto.ComplaintSummaryDTO;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GlobalSearchService {

    private final ComplaintRepository complaintRepository;
    private final OfficerProfileRepository officerProfileRepository;

    // Admin - Search all complaints
    public Page<ComplaintSummaryDTO> searchAllComplaints(
            String query,
            Long wardId,
            Long departmentId,
            Pageable pageable) {

        if (wardId != null && departmentId != null) {
            return complaintRepository
                    .findByWard_WardIdAndDepartment_DepartmentIdAndTitleContainingIgnoreCase(
                            wardId, departmentId, query, pageable)
                    .map(this::toSummaryDTO);
        } else if (wardId != null) {
            return complaintRepository
                    .findByWard_WardIdAndTitleContainingIgnoreCase(wardId, query, pageable)
                    .map(this::toSummaryDTO);
        } else if (departmentId != null) {
            return complaintRepository
                    .findByDepartment_DepartmentIdAndTitleContainingIgnoreCase(departmentId, query, pageable)
                    .map(this::toSummaryDTO);
        } else {
            return complaintRepository
                    .search(query, pageable)
                    .map(this::toSummaryDTO);
        }
    }

    // Ward Officer - Search complaints in their ward
    public Page<ComplaintSummaryDTO> searchWardComplaints(
            String query,
            Long wardOfficerUserId,
            Long departmentId,
            Pageable pageable) {

        OfficerProfile profile = officerProfileRepository
                .findByUser_UserId(wardOfficerUserId)
                .orElseThrow();

        Long wardId = profile.getWard().getWardId();

        if (departmentId != null) {
            return complaintRepository
                    .findByWard_WardIdAndDepartment_DepartmentIdAndTitleContainingIgnoreCase(
                            wardId, departmentId, query, pageable)
                    .map(this::toSummaryDTO);
        } else {
            return complaintRepository
                    .findByWard_WardIdAndTitleContainingIgnoreCase(wardId, query, pageable)
                    .map(this::toSummaryDTO);
        }
    }

    // Department Officer - Search their assigned complaints
    public Page<ComplaintSummaryDTO> searchDepartmentComplaints(
            String query,
            Long departmentOfficerUserId,
            Pageable pageable) {

        return complaintRepository
                .findByAssignedOfficer_UserIdAndTitleContainingIgnoreCase(
                        departmentOfficerUserId, query, pageable)
                .map(this::toSummaryDTO);
    }

    // Citizen - Search their own complaints
    public Page<ComplaintSummaryDTO> searchCitizenComplaints(
            String query,
            Long citizenUserId,
            Pageable pageable) {

        return complaintRepository
                .findByCitizen_UserIdAndTitleContainingIgnoreCase(
                        citizenUserId, query, pageable)
                .map(this::toSummaryDTO);
    }

    private ComplaintSummaryDTO toSummaryDTO(com.example.CivicConnect.entity.complaint.Complaint complaint) {
        return new ComplaintSummaryDTO(
                complaint.getComplaintId(),
                complaint.getTitle(),
                complaint.getStatus(),
                complaint.getCreatedAt()
        );
    }
}
