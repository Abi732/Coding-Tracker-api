package com.abi.coding_tracker.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abi.coding_tracker.dashboard.dto.DashboardResponse;
import com.abi.coding_tracker.dashboard.service.DashboardService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService){
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getMyDashboard(Authentication authentication){
        String email = authentication.getName();
        log.info("User [{}] requested unified dashboard data", email);

        DashboardResponse response = dashboardService.getDashboardData(email);

        return ResponseEntity.ok(response);
    }
}
