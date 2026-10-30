package com.chronotrack.service;

import com.chronotrack.dto.ReminderRequest;
import com.chronotrack.dto.ReminderResponse;
import com.chronotrack.entity.Reminder;
import com.chronotrack.entity.User;
import com.chronotrack.exception.ResourceNotFoundException;
import com.chronotrack.repository.ReminderRepository;
import com.chronotrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing user notifications and habit reminders.
 */
@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    public ReminderService(ReminderRepository reminderRepository, UserRepository userRepository) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReminderResponse createReminder(Long userId, ReminderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Reminder reminder = new Reminder(
                user,
                request.getTitle().trim(),
                request.getReminderTime().trim(),
                request.getEnabled() != null ? request.getEnabled() : true
        );

        Reminder saved = reminderRepository.save(reminder);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getReminders(Long userId) {
        return reminderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ReminderResponse updateReminder(Long reminderId, Long userId, ReminderRequest request) {
        Reminder reminder = reminderRepository.findByIdAndUserId(reminderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + reminderId));

        reminder.setTitle(request.getTitle().trim());
        reminder.setReminderTime(request.getReminderTime().trim());
        if (request.getEnabled() != null) {
            reminder.setEnabled(request.getEnabled());
        }

        Reminder updated = reminderRepository.save(reminder);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteReminder(Long reminderId, Long userId) {
        Reminder reminder = reminderRepository.findByIdAndUserId(reminderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + reminderId));
        reminderRepository.delete(reminder);
    }

    private ReminderResponse mapToResponse(Reminder reminder) {
        return new ReminderResponse(
                reminder.getId(),
                reminder.getTitle(),
                reminder.getReminderTime(),
                reminder.isEnabled(),
                reminder.getCreatedAt()
        );
    }
}
