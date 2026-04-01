package com.example.ai_agent_auto_call_answer.repository;

import com.example.ai_agent_auto_call_answer.entity.CandidateProfile;
import com.example.ai_agent_auto_call_answer.entity.CandidateResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateResumeRepository
        extends JpaRepository<CandidateResume, Long> {

    List<CandidateResume> findByCandidate(
            CandidateProfile candidate);

    Optional<CandidateResume> findByCandidateAndIsDefaultTrue(
            CandidateProfile candidate);

    List<CandidateResume> findByCandidateAndResumeType(
            CandidateProfile candidate, String resumeType);
}