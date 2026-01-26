package com.example.CivicConnect.controller.admincomplaint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.entity.core.User;
import com.example.CivicConnect.entity.enums.RoleName;
import com.example.CivicConnect.service.admincomplaint.AdminAuditService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
public class AdminAuditController {

    private final AdminAuditService auditService;

    @GetMapping("/logs")
    public ResponseEntity<?> viewAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long userId,
            Pageable pageable,
            Authentication auth) {

        User admin = (User) auth.getPrincipal();
        
        if (admin.getRole() != RoleName.ADMIN) {
            throw new RuntimeException("Access denied");
        }

        return ResponseEntity.ok(
                auditService.getAuditLogs(action, entityType, userId, pageable)
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getAuditSummary(Authentication auth) {
        User admin = (User) auth.getPrincipal();
        
        if (admin.getRole() != RoleName.ADMIN) {
            throw new RuntimeException("Access denied");
        }

        return ResponseEntity.ok(auditService.getAuditSummary());
    }
}
