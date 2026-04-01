package com.example.ai_agent_auto_call_answer.tools;

import com.example.ai_agent_auto_call_answer.entity.CandidateResume;
import com.example.ai_agent_auto_call_answer.repository.CandidateProfileRepository;
import com.example.ai_agent_auto_call_answer.repository.CandidateResumeRepository;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumePickerTool {

    private final CandidateProfileRepository profileRepository;
    private final CandidateResumeRepository resumeRepository;

    @Tool("Pick the best matching resume based on the role the recruiter mentioned")
    public String getBestResumeForRole(String roleDescription) {

        var candidate = profileRepository.findById(1L)
                .orElseThrow(() ->
                        new RuntimeException("No candidate found"));

        List<CandidateResume> allResumes = resumeRepository
                .findByCandidate(candidate);

        if (allResumes.isEmpty()) {
            return "No resume found in the system";
        }

        String roleLower = roleDescription.toLowerCase();

        // Smart keyword matching
        for (CandidateResume resume : allResumes) {
            String[] keywords = resume.getKeywords()
                    .toLowerCase().split(",");

            for (String keyword : keywords) {
                if (roleLower.contains(keyword.trim())) {
                    log.info("Matched resume: {} for role: {}",
                            resume.getResumeLabel(), roleDescription);
                    return resume.getResumeText();
                }
            }
        }

        // No keyword match — return default resume
        log.info("No keyword match found, returning default resume");
        return resumeRepository
                .findByCandidateAndIsDefaultTrue(candidate)
                .map(CandidateResume::getResumeText)
                .orElse(allResumes.get(0).getResumeText());
    }

    @Tool("Get a list of all available resume types for this candidate")
    public String getAllResumeTypes() {

        var candidate = profileRepository.findById(1L)
                .orElseThrow(() ->
                        new RuntimeException("No candidate found"));

        List<CandidateResume> resumes = resumeRepository
                .findByCandidate(candidate);

        if (resumes.isEmpty()) {
            return "No resumes uploaded yet";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Available resumes:\n");

        for (CandidateResume resume : resumes) {
            sb.append("- ")
                    .append(resume.getResumeLabel())
                    .append(" (keywords: ")
                    .append(resume.getKeywords())
                    .append(")")
                    .append(resume.getIsDefault() ? " [DEFAULT]" : "")
                    .append("\n");
        }

        return sb.toString();
    }

    @Tool("Get the default resume when no specific role is mentioned")
    public String getDefaultResume() {

        var candidate = profileRepository.findById(1L)
                .orElseThrow(() ->
                        new RuntimeException("No candidate found"));

        return resumeRepository
                .findByCandidateAndIsDefaultTrue(candidate)
                .map(resume -> {
                    log.info("Returning default resume: {}",
                            resume.getResumeLabel());
                    return resume.getResumeText();
                })
                .orElse("No default resume set");
    }
}