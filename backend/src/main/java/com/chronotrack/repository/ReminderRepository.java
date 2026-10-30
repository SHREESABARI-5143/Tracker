package com.chronotrack.repository;

import com.chronotrack.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Reminder> findByUserIdAndEnabledTrue(Long userId);

    Optional<Reminder> findByIdAndUserId(Long id, Long userId);
}
