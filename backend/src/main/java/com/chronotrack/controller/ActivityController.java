package com.chronotrack.controller;

import com.chronotrack.dto.ActivityRequest;
import com.chronotrack.dto.ActivityResponse;
import com.chronotrack.dto.ApiResponse;
import com.chronotrack.security.CustomUserDetails;
import com.chronotrack.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * REST controller for Activity management, overlap checking, and filtered querying.
 */
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /**
     * Create a new activity manually after checking for non-overlapping intervals.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ActivityResponse>> createActivity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ActivityRequest request) {
        ActivityResponse response = activityService.createActivity(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Activity logged successfully.", response));
    }

    /**
     * Get paginated activities for the authenticated user with optional category, search keyword, and date filters.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> getActivities(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<ActivityResponse> activities = activityService.getUserActivities(
                userDetails.getId(), categoryId, search, startTime, endTime, pageable);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }

    /**
     * Get a single activity by ID, verifying user ownership.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivity(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ActivityResponse activity = activityService.getActivity(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(activity));
    }

    /**
     * Update an activity by ID, validating non-overlapping interval (excluding self).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityResponse>> updateActivity(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ActivityRequest request) {
        ActivityResponse updated = activityService.updateActivity(id, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Activity updated successfully.", updated));
    }

    /**
     * Delete an activity by ID, verifying user ownership.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteActivity(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        activityService.deleteActivity(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Activity deleted successfully.", null));
    }
}
