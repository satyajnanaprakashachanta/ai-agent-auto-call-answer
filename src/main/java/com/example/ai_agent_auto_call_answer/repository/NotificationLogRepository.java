package com.example.ai_agent_auto_call_answer.repository;

import com.example.ai_agent_auto_call_answer.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationLogRepository
        extends JpaRepository<NotificationLog, Long> {
}