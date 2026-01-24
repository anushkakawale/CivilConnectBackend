package com.example.CivicConnect.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CivicConnect.dto.ChartDTO;
import com.example.CivicConnect.service.admin.AdminChartService;

@RestController
@RequestMapping("/api/admin/charts")
public class AdminChartController {

    private final AdminChartService service;

    public AdminChartController(AdminChartService service) {
        this.service = service;
    }

    @GetMapping("/complaints-by-ward")
    public List<ChartDTO> byWard() {
        return service.complaintsByWard();
    }

    @GetMapping("/complaints-by-department")
    public List<ChartDTO> byDepartment() {
        return service.complaintsByDepartment();
    }

    @GetMapping("/sla-stats")
    public List<ChartDTO> slaStats() {
        return service.slaStats();
    }
}
