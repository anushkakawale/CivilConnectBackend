package com.example.CivicConnect.service.departmentcomplaint;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.CivicConnect.entity.complaint.Complaint;
import com.example.CivicConnect.entity.complaint.ComplaintApproval;
import com.example.CivicConnect.entity.complaint.ComplaintStatusHistory;
import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.ApprovalStatus;
import com.example.CivicConnect.entity.enums.ComplaintStatus;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.repository.ComplaintApprovalRepository;
import com.example.CivicConnect.repository.ComplaintRepository;
import com.example.CivicConnect.repository.ComplaintStatusHistoryRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class DepartmentComplaintService {

	private final ComplaintRepository complaintRepository;
	private final ComplaintStatusHistoryRepository historyRepository;
	private final ComplaintApprovalRepository approvalRepository;

	public DepartmentComplaintService(ComplaintRepository complaintRepository,
			ComplaintStatusHistoryRepository historyRepository, ComplaintApprovalRepository approvalRepository) {

		this.complaintRepository = complaintRepository;
		this.historyRepository = historyRepository;
		this.approvalRepository = approvalRepository;
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
	}

	// ▶ RESOLVE COMPLAINT
	public void resolve(Long complaintId, User officer) {

		Complaint complaint = getComplaint(complaintId);

		if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS) {
			throw new RuntimeException("Must be IN_PROGRESS");
		}

		complaint.setStatus(ComplaintStatus.RESOLVED);
		logStatus(complaint, ComplaintStatus.RESOLVED, officer);

		// 🔔 CREATE PENDING APPROVAL FOR WARD OFFICER
		ComplaintApproval approval = new ComplaintApproval();
		approval.setComplaint(complaint);
		approval.setStatus(ApprovalStatus.PENDING);
		approval.setRoleAtTime(RoleName.WARD_OFFICER);
		approval.setDecidedAt(LocalDateTime.now()); // when approval was created

		approvalRepository.save(approval);
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
