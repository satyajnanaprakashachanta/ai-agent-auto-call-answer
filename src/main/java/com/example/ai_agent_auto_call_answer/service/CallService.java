package com.example.ai_agent_auto_call_answer.service;

import com.example.ai_agent_auto_call_answer.entity.CallLog;
import com.example.ai_agent_auto_call_answer.entity.Transcript;
import com.example.ai_agent_auto_call_answer.repository.CallLogRepository;
import com.example.ai_agent_auto_call_answer.repository.TranscriptRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CallService {

    private final CallLogRepository callLogRepository;
    private final TranscriptRepository transcriptRepository;

    public CallLog createCallLog(
            String callSid, String recruiterPhone) {

        CallLog callLog = CallLog.builder()
                .callSid(callSid)
                .recruiterPhone(recruiterPhone)
                .build();

        log.info("New call started. SID: {}", callSid);
        return callLogRepository.save(callLog);
    }

    public void saveTranscript(
            CallLog callLog,
            String speaker,
            String message) {

        Transcript transcript = Transcript.builder()
                .callLog(callLog)
                .speaker(speaker)
                .messageText(message)
                .build();

        transcriptRepository.save(transcript);
    }

    public void updateCallDetails(
            String callSid,
            String companyName,
            String roleTitle,
            String salaryRange,
            String workLocation,
            Boolean sponsorshipAvailable) {

        callLogRepository.findByCallSid(callSid)
                .ifPresent(call -> {
                    call.setCompanyName(companyName);
                    call.setRoleTitle(roleTitle);
                    call.setSalaryRange(salaryRange);
                    call.setWorkLocation(workLocation);
                    call.setSponsorshipAvailable(
                            sponsorshipAvailable);
                    callLogRepository.save(call);
                });
    }

    public void endCall(String callSid, String summary) {
        callLogRepository.findByCallSid(callSid)
                .ifPresent(call -> {
                    call.setCallStatus("completed");
                    call.setSummary(summary);
                    callLogRepository.save(call);
                    log.info("Call ended. SID: {}", callSid);
                });
    }

    public CallLog getCallBySid(String callSid) {
        return callLogRepository.findByCallSid(callSid)
                .orElseThrow(() ->
                        new RuntimeException("Call not found: "
                                + callSid));
    }
}