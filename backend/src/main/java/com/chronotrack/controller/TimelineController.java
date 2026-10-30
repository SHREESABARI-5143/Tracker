package com.chronotrack.controller;

import com.chronotrack.dto.ApiResponse;
import com.chronotrack.dto.DailyTimelineResponse;
import com.chronotrack.security.CustomUserDetails;
import com.chronotrack.service.TimelineService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * REST controller for retrieving 24-hour daily timeline schedules and metrics.
 */
@RestController
@RequestMapping("/api/timeline")
public class TimelineController {

    private final TimelineService timelineService;

    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    /**
     * Get 24-hour daily timeline for a specific date (defaults to today).
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<DailyTimelineResponse>> getDailyTimeline(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyTimelineResponse timeline = timelineService.getDailyTimeline(userDetails.getId(), date);
        return ResponseEntity.ok(ApiResponse.success(timeline));
    }
}
