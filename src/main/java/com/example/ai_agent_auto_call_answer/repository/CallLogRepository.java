package com.example.ai_agent_auto_call_answer.repository;

import com.example.ai_agent_auto_call_answer.entity.CallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CallLogRepository
        extends JpaRepository<CallLog, Long> {

    Optional<CallLog> findByCallSid(String callSid);
}