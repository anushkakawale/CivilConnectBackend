package com.example.CivicConnect.controller.admincomplaint;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import com.example.CivicConnect.service.admincomplaint.AdminAuditService;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditController {

    private final AdminAuditService auditService;

    @GetMapping("/logs")
    public ResponseEntity<?> viewAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long userId,
            Pageable pageable) {

        return ResponseEntity.ok(
                auditService.getAuditLogs(action, entityType, userId, pageable)
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getAuditSummary() {
        return ResponseEntity.ok(auditService.getAuditSummary());
    }
}
