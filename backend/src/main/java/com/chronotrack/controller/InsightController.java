package com.chronotrack.controller;

import com.chronotrack.dto.ApiResponse;
import com.chronotrack.dto.InsightResponse;
import com.chronotrack.security.CustomUserDetails;
import com.chronotrack.service.InsightService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for retrieving data-driven productivity insights and habit analytics.
 */
@RestController
@RequestMapping("/api/insights")
public class InsightController {

    private final InsightService insightService;

    public InsightController(InsightService insightService) {
        this.insightService = insightService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InsightResponse>>> getInsights(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<InsightResponse> insights = insightService.generateInsights(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(insights));
    }
}
