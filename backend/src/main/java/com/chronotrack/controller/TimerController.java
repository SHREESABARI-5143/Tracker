package com.chronotrack.controller;

import com.chronotrack.dto.ActivityResponse;
import com.chronotrack.dto.ApiResponse;
import com.chronotrack.dto.TimerResponse;
import com.chronotrack.dto.TimerStartRequest;
import com.chronotrack.security.CustomUserDetails;
import com.chronotrack.service.TimerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing database-persisted activity timers.
 */
@RestController
@RequestMapping("/api/timer")
public class TimerController {

    private final TimerService timerService;

    public TimerController(TimerService timerService) {
        this.timerService = timerService;
    }

    /**
     * Start a new persistent activity timer.
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<TimerResponse>> startTimer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TimerStartRequest request) {
        TimerResponse response = timerService.startTimer(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Timer started successfully.", response));
    }

    /**
     * Pause the running timer.
     */
    @PostMapping("/pause")
    public ResponseEntity<ApiResponse<TimerResponse>> pauseTimer(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        TimerResponse response = timerService.pauseTimer(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Timer paused.", response));
    }

    /**
     * Resume a paused timer.
     */
    @PostMapping("/resume")
    public ResponseEntity<ApiResponse<TimerResponse>> resumeTimer(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        TimerResponse response = timerService.resumeTimer(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Timer resumed.", response));
    }

    /**
     * Stop the timer, convert it to a completed Activity record, and remove the timer session.
     */
    @PostMapping("/stop")
    public ResponseEntity<ApiResponse<ActivityResponse>> stopTimer(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ActivityResponse activity = timerService.stopTimer(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Timer stopped and activity logged successfully.", activity));
    }

    /**
     * Get currently active running or paused timer session (returns null data if no timer is active).
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<TimerResponse>> getCurrentTimer(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        TimerResponse response = timerService.getCurrentTimer(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
