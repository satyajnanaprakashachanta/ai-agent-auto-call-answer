package com.example.ai_agent_auto_call_answer.tools;

import com.example.ai_agent_auto_call_answer.entity.CallLog;
import com.example.ai_agent_auto_call_answer.entity.Transcript;
import com.example.ai_agent_auto_call_answer.repository.CallLogRepository;
import com.example.ai_agent_auto_call_answer.repository.TranscriptRepository;
import com.example.ai_agent_auto_call_answer.service.ProfileService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CandidateTools {

    private final ProfileService profileService;
    private final CallLogRepository callLogRepository;
    private final TranscriptRepository transcriptRepository;

    @Tool("Get the full resume content that best matches the role description mentioned by the recruiter")
    public String getBestResume(String roleDescription) {
        log.info("Getting best resume for role: {}", roleDescription);
        return profileService.getBestResumeForRole(roleDescription);
    }

    @Tool("Get candidate preferences like salary, sponsorship, work preference and notice period")
    public String getCandidatePreferences() {
        var profile = profileService.getProfile();
        return String.format("""
                Salary expectation: $%s to $%s per year
                Needs visa sponsorship: %s
                Work preference: %s
                Notice period: %s
                Preferred locations: %s
                """,
                profile.getSalaryMin(),
                profile.getSalaryMax(),
                profile.getNeedsSponsorship() ? "Yes" : "No",
                profile.getWorkPreference(),
                profile.getNoticePeriod(),
                profile.getPreferredLocations()
        );
    }

    @Tool("Save the job details collected from the recruiter during the call")
    public String saveJobDetails(
            String callSid,
            String companyName,
            String roleTitle,
            String salaryRange,
            String workLocation,
            String sponsorshipAvailable) {

        callLogRepository.findByCallSid(callSid).ifPresent(call -> {
            call.setCompanyName(companyName);
            call.setRoleTitle(roleTitle);
            call.setSalaryRange(salaryRange);
            call.setWorkLocation(workLocation);
            call.setSponsorshipAvailable(
                    "yes".equalsIgnoreCase(sponsorshipAvailable));
            callLogRepository.save(call);
            log.info("Job details saved for call: {}", callSid);
        });

        return "Job details saved successfully";
    }

    @Tool("Save a message to the call transcript")
    public String saveToTranscript(
            String callSid,
            String speaker,
            String message) {

        callLogRepository.findByCallSid(callSid).ifPresent(call -> {
            Transcript transcript = Transcript.builder()
                    .callLog(call)
                    .speaker(speaker)
                    .messageText(message)
                    .build();
            transcriptRepository.save(transcript);
        });

        return "Saved to transcript";
    }

    @Tool("Get the full transcript of the current call")
    public String getCallTranscript(String callSid) {
        return callLogRepository.findByCallSid(callSid)
                .map(call -> {
                    var transcripts = transcriptRepository
                            .findByCallLogOrderByTimestampAsc(call);
                    StringBuilder sb = new StringBuilder();
                    for (Transcript t : transcripts) {
                        sb.append(t.getSpeaker())
                                .append(": ")
                                .append(t.getMessageText())
                                .append("\n");
                    }
                    return sb.toString();
                })
                .orElse("No transcript found");
    }
}