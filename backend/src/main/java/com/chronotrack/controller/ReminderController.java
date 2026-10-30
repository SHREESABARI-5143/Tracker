package com.chronotrack.controller;

import com.chronotrack.dto.ApiResponse;
import com.chronotrack.dto.ReminderRequest;
import com.chronotrack.dto.ReminderResponse;
import com.chronotrack.security.CustomUserDetails;
import com.chronotrack.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user reminders and notifications.
 */
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReminderResponse>> createReminder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReminderRequest request) {
        ReminderResponse response = reminderService.createReminder(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reminder created successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReminderResponse>>> getReminders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ReminderResponse> reminders = reminderService.getReminders(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(reminders));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReminderResponse>> updateReminder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReminderRequest request) {
        ReminderResponse response = reminderService.updateReminder(id, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Reminder updated successfully.", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReminder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reminderService.deleteReminder(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Reminder deleted successfully.", null));
    }
}
