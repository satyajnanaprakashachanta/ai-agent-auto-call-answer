package com.example.ai_agent_auto_call_answer.repository;

import com.example.ai_agent_auto_call_answer.entity.CallLog;
import com.example.ai_agent_auto_call_answer.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TranscriptRepository
        extends JpaRepository<Transcript, Long> {

    List<Transcript> findByCallLogOrderByTimestampAsc(
            CallLog callLog);
}