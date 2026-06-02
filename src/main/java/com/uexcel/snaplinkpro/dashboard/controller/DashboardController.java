package com.uexcel.snaplinkpro.dashboard.controller;

import com.uexcel.snaplinkpro.dashboard.dto.DashboardSummaryResponse;
import com.uexcel.snaplinkpro.dashboard.service.DashboardService;
import com.uexcel.snaplinkpro.dto.ResponseUtil;
import com.uexcel.snaplinkpro.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            Authentication authentication) {

        return ResponseEntity.ok(
                ResponseUtil.success(
                        dashboardService.getSummary(authentication.getName())
                )
        );
    }
}
