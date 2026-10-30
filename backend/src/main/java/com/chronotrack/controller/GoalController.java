package com.chronotrack.controller;

import com.chronotrack.dto.ApiResponse;
import com.chronotrack.dto.GoalRequest;
import com.chronotrack.dto.GoalResponse;
import com.chronotrack.security.CustomUserDetails;
import com.chronotrack.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller managing personal productivity goals and progress tracking.
 */
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GoalRequest request) {
        GoalResponse response = goalService.createGoal(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goal created successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoals(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<GoalResponse> goals = goalService.getGoals(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> getGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GoalResponse response = goalService.getGoal(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GoalRequest request) {
        GoalResponse response = goalService.updateGoal(id, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Goal updated successfully.", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        goalService.deleteGoal(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Goal deleted successfully.", null));
    }
}
