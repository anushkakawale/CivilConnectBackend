package com.example.CivicConnect.service.admincomplaint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.repository.AccessLogRepository;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintSlaRepository;
import com.example.CivicConnect.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintSlaRepository slaRepository;
    private final UserRepository userRepository;
    private final AccessLogRepository accessLogRepository;

    public Map<String, Object> summaryReport() {

        return Map.of(
            "totalComplaints", complaintRepository.count(),
            "closedComplaints",
                complaintRepository.countByStatus(ComplaintStatus.CLOSED),
            "slaBreached",
                slaRepository.countByStatus(SLAStatus.BREACHED),
            "slaMet",
                slaRepository.countByStatus(SLAStatus.MET),
            "totalUsers",
                userRepository.count()
        );
    }

    public List<Complaint> detailedComplaints() {
        return complaintRepository.findAll();
    }

    public Map<String, Object> slaPerformance() {
        return Map.of(
            "total", slaRepository.count(),
            "active", slaRepository.countByStatus(SLAStatus.ACTIVE),
            "breached", slaRepository.countByStatus(SLAStatus.BREACHED),
            "met", slaRepository.countByStatus(SLAStatus.MET)
        );
    }

    public Map<String, Object> userActivity() {
        return Map.of(
            "totalUsers", userRepository.count(),
            "loginsLast24h",
            accessLogRepository
                .findByCreatedAtAfter(LocalDateTime.now().minusHours(24))
                .size()
        );
    }
}
