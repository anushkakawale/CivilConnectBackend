package com.example.CivicConnect.service.citizencomplaint;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.dto.ComplaintTimelineDTO;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CitizenComplaintQueryService {

    private final ComplaintStatusHistoryRepository historyRepository;

    public List<ComplaintTimelineDTO> getTimeline(Long complaintId) {
        return historyRepository
                .findByComplaint_ComplaintIdOrderByChangedAtAsc(complaintId)
                .stream()
                .map(h -> new ComplaintTimelineDTO(
                        h.getStatus().name(),
                        h.getChangedBy().getRole().name(),
                        h.getChangedAt()
                ))
                .toList();
    }
}
