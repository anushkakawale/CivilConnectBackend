package com.example.CivicConnect.service.departmentcomplaint;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintApproval;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.NotificationType;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.entity.enums.SLAStatus;
import com.example.CivicConnect.repository.ComplaintApprovalRepository;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintSlaRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;
import com.example.CivicConnect.service.NotificationService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class DepartmentComplaintService {

	private final ComplaintRepository complaintRepository;
	private final ComplaintStatusHistoryRepository historyRepository;
	private final ComplaintApprovalRepository approvalRepository;
	private final NotificationService notificationService;
	private final ComplaintSlaRepository slaRepository;

	public DepartmentComplaintService(
	        ComplaintRepository complaintRepository,
	        ComplaintStatusHistoryRepository historyRepository,
	        ComplaintApprovalRepository approvalRepository,
	        NotificationService notificationService,
	        ComplaintSlaRepository slaRepository) {

	    this.complaintRepository = complaintRepository;
	    this.historyRepository = historyRepository;
	    this.approvalRepository = approvalRepository;
	    this.notificationService = notificationService;
	    this.slaRepository = slaRepository;
	}

	// ▶ START WORK
	public void startWork(Long complaintId, User officer) {

		Complaint complaint = getComplaint(complaintId);

		if (!officer.getUserId().equals(complaint.getAssignedOfficer().getUserId())) {
			throw new RuntimeException("You are not assigned");
		}

		if (complaint.getStatus() != ComplaintStatus.ASSIGNED) {
			throw new RuntimeException("Invalid state");
		}

		complaint.setStatus(ComplaintStatus.IN_PROGRESS);
		logStatus(complaint, ComplaintStatus.IN_PROGRESS, officer);

		notificationService.notifyCitizen(
		    complaint.getCitizen(),
		    "Work Started",
		    "Work has started on your complaint " + complaint.getComplaintId(),
		    complaint.getComplaintId(),
		    NotificationType.STATUS_UPDATE
		);

		// TODO: Re-enable when User has ward relationship
		/*
		notificationService.notifyWardOfficer(
			    complaint.getWard().getWardId(),
			    "Work Started",
			    "Complaint " + complaint.getComplaintId() + " work has started",
			    complaint.getComplaintId(),
			    NotificationType.STATUS_UPDATE
			);
		*/


	}

	// ▶ RESOLVE COMPLAINT
	// ▶ RESOLVE COMPLAINT
	public void resolve(Long complaintId, User officer) {

	    Complaint complaint = getComplaint(complaintId);

	    if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS) {
	        throw new RuntimeException("Must be IN_PROGRESS");
	    }

	    // 1️⃣ Update complaint
	    complaint.setStatus(ComplaintStatus.RESOLVED);
	    logStatus(complaint, ComplaintStatus.RESOLVED, officer);

	    // 2️⃣ Stop SLA
	    slaRepository.findByComplaint(complaint).ifPresent(sla -> {
	        sla.setSlaEndTime(LocalDateTime.now());
	        sla.setStatus(SLAStatus.MET);
	        slaRepository.save(sla);
	    });

	    // 3️⃣ Create approval for Ward Officer
	    ComplaintApproval approval = new ComplaintApproval();
	    approval.setComplaint(complaint);
	    approval.setStatus(ApprovalStatus.PENDING);
	    approval.setRoleAtTime(RoleName.WARD_OFFICER);
	    approval.setDecidedAt(LocalDateTime.now());

	    approvalRepository.save(approval);

	    // 4️⃣ 🔔 Notify Ward Officer
	    // TODO: Re-enable when User has ward relationship
	    /*
	    notificationService.notifyWardOfficer(
	    	    complaint.getWard().getWardId(),
	    	    "Approval Required",
	    	    "Complaint " + complaint.getComplaintId() + " resolved and awaiting approval",
	    	    complaint.getComplaintId(),
	    	    NotificationType.STATUS_UPDATE
	    	);
	    */
	}


	private Complaint getComplaint(Long id) {
		return complaintRepository.findById(id).orElseThrow(() -> new RuntimeException("Complaint not found"));
	}

	private void logStatus(Complaint complaint, ComplaintStatus status, User user) {

		ComplaintStatusHistory history = new ComplaintStatusHistory();
		history.setComplaint(complaint);
		history.setStatus(status);
		history.setChangedBy(user);
		history.setChangedAt(LocalDateTime.now());
		historyRepository.save(history);
	}
}
