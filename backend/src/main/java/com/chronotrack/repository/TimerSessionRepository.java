package com.chronotrack.repository;

import com.chronotrack.entity.TimerSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimerSessionRepository extends JpaRepository<TimerSession, Long> {

    Optional<TimerSession> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
