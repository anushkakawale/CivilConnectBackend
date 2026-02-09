package com.example.CivicConnect.controller.analytics;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.service.SlaAnalyticsService;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class SlaAnalyticsController {

    private final SlaAnalyticsService service;

    public SlaAnalyticsController(SlaAnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/sla/overall")
    public ResponseEntity<?> overall() {
        return ResponseEntity.ok(service.overallReport());
    }

    @GetMapping("/sla/department/{departmentId}")
    public ResponseEntity<?> department(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(
                service.departmentReport(departmentId)
        );
    }
}
