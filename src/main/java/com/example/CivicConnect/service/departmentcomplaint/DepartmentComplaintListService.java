package com.example.CivicConnect.service.departmentcomplaint;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.entity.profiles.OfficerProfile;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.OfficerProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentComplaintListService {

    private final ComplaintRepository complaintRepository;
    private final OfficerProfileRepository officerProfileRepository;

    public Map<String, Object> getDepartmentComplaints(
            User officer,
            int page,
            int size,
            ComplaintStatus status,
            SLAStatus slaStatus
    ) {

        OfficerProfile profile =
                officerProfileRepository
                        .findByUser_UserId(officer.getUserId())
                        .orElseThrow(() ->
                                new RuntimeException("Officer profile not found"));

        Long wardId = profile.getWard().getWardId();
        Long departmentId = profile.getDepartment().getDepartmentId();

        Pageable pageable =
                PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Complaint> complaintPage;

        if (status != null && slaStatus != null) {
            complaintPage =
                    complaintRepository
                            .findByWard_WardIdAndDepartment_DepartmentIdAndStatusAndSla_Status(
                                    wardId,
                                    departmentId,
                                    status,
                                    slaStatus,
                                    pageable
                            );

        } else if (slaStatus != null) {
            complaintPage =
                    complaintRepository
                            .findByWard_WardIdAndDepartment_DepartmentIdAndSla_Status(
                                    wardId,
                                    departmentId,
                                    slaStatus,
                                    pageable
                            );

        } else if (status != null) {
            complaintPage =
                    complaintRepository
                            .findByWard_WardIdAndDepartment_DepartmentIdAndStatus(
                                    wardId,
                                    departmentId,
                                    status,
                                    pageable
                            );

        } else {
            complaintPage =
                    complaintRepository
                            .findByWard_WardIdAndDepartment_DepartmentId(
                                    wardId,
                                    departmentId,
                                    pageable
                            );
        }

        return buildResponse(complaintPage);
    }

    private Map<String, Object> buildResponse(Page<Complaint> page) {

        Map<String, Object> response = new HashMap<>();

        response.put("content",
                page.getContent()
                        .stream()
                        .map(this::mapComplaintToDTO)
                        .toList());

        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("currentPage", page.getNumber());
        response.put("pageSize", page.getSize());

        return response;
    }

    private Map<String, Object> mapComplaintToDTO(Complaint complaint) {

        Map<String, Object> dto = new HashMap<>();

        dto.put("complaintId", complaint.getComplaintId());
        dto.put("title", complaint.getTitle());
        dto.put("status", complaint.getStatus().name());
        dto.put("priority", complaint.getPriority().name());
        dto.put("ward", complaint.getWard().getAreaName());
        dto.put("department", complaint.getDepartment().getName());

        if (complaint.getSla() != null) {
            dto.put("slaStatus", complaint.getSla().getStatus().name());
            dto.put("slaDeadline", complaint.getSla().getSlaDeadline());
        }

        return dto;
    }
}
