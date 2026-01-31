package com.example.CivicConnect.service.admincomplaint;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.repository.ComplaintRepository;

import jakarta.annotation.Priority;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminComplaintListService {

    private final ComplaintRepository complaintRepository;

    public Map<String, Object> getAllComplaints(
            int page,
            int size,
            ComplaintStatus status,
            Priority priority,
            SLAStatus slaStatus
    ) {

        Pageable pageable =
                PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Complaint> complaintPage;

        if (slaStatus != null && status != null) {
            complaintPage =
                    complaintRepository
                            .findByStatusAndSla_Status(
                                    status,
                                    slaStatus,
                                    pageable
                            );

        } else if (slaStatus != null) {
            complaintPage =
                    complaintRepository
                            .findBySla_Status(
                                    slaStatus,
                                    pageable
                            );

        } else if (status != null) {
            complaintPage =
                    complaintRepository
                            .findByStatus(
                                    status,
                                    pageable
                            );

        } else {
            complaintPage =
                    complaintRepository.findAll(pageable);
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
