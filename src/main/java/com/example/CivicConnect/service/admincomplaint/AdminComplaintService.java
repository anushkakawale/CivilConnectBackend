package com.example.CivicConnect.service.admincomplaint;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.entity.sla.ComplaintSla;
import com.example.CivicConnect.entity.system.Notification;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintSlaRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;
import com.example.CivicConnect.repository.NotificationRepository;
import com.example.CivicConnect.service.system.AccessLogService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final NotificationRepository notificationRepository;
    private final ComplaintSlaRepository slaRepository;
    private final AccessLogService accessLogService;

    public AdminComplaintService(
            ComplaintRepository complaintRepository,
            ComplaintStatusHistoryRepository historyRepository,
            NotificationRepository notificationRepository,
            ComplaintSlaRepository slaRepository) {

        this.complaintRepository = complaintRepository;
        this.historyRepository = historyRepository;
        this.notificationRepository = notificationRepository;
        this.slaRepository = slaRepository;
		this.accessLogService = null;
    }
 // ✅ PAGINATED LIST (NEW)
    public Page<Complaint> getAllComplaints(Pageable pageable) {
        return complaintRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    public void closeComplaint(Long complaintId, User admin) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (complaint.getStatus() != ComplaintStatus.APPROVED) {
            throw new RuntimeException("Only APPROVED complaints can be CLOSED");
        }

        // ✅ CLOSE COMPLAINT
        complaint.setStatus(ComplaintStatus.CLOSED);
        complaint.setClosedAt(LocalDateTime.now());
        complaint.setClosedByAdmin(admin);
        complaint.setLastUpdatedBy(admin);
        complaint.setUpdatedAt(LocalDateTime.now());

        // ✅ CLOSE SLA
        ComplaintSla sla = slaRepository
                .findByComplaint_ComplaintId(complaintId)
                .orElseThrow(() -> new RuntimeException("SLA not found"));

        sla.setSlaEndTime(LocalDateTime.now());

        if (LocalDateTime.now().isAfter(sla.getSlaDeadline())) {
            sla.setStatus(SLAStatus.BREACHED);
            sla.setEscalated(true);
            complaint.setSlaBreached(true);
        } else {
            sla.setStatus(SLAStatus.MET);
        }

        // ✅ STATUS HISTORY
        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setStatus(ComplaintStatus.CLOSED);
        history.setChangedBy(admin);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);

        // ✅ NOTIFY CITIZEN
        Notification n = new Notification();
        n.setUser(complaint.getCitizen());
        n.setMessage("Your complaint has been CLOSED by Admin");
        n.isRead();
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);

        complaintRepository.save(complaint);
        slaRepository.save(sla);
       
        accessLogService.log(
        	    admin,
        	    "CLOSE_COMPLAINT",
        	    "COMPLAINT",
        	    complaintId,
        	    "SYSTEM"
        	);

    }
}


