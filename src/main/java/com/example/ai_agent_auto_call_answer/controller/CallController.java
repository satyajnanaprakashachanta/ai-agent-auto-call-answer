package com.example.ai_agent_auto_call_answer.controller;

import com.example.ai_agent_auto_call_answer.agent.AgentConfig;
import com.example.ai_agent_auto_call_answer.agent.ConversationAgent;
import com.example.ai_agent_auto_call_answer.agent.SummaryAgent;
import com.example.ai_agent_auto_call_answer.entity.CallLog;
import com.example.ai_agent_auto_call_answer.service.CallService;
import com.example.ai_agent_auto_call_answer.service.NotificationService;
import com.example.ai_agent_auto_call_answer.service.ProfileService;
import com.example.ai_agent_auto_call_answer.service.VoiceService;
import com.example.ai_agent_auto_call_answer.tools.CandidateTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/call")
@RequiredArgsConstructor
@Slf4j
public class CallController {

    private final CallService callService;
    private final VoiceService voiceService;
    private final ProfileService profileService;
    private final NotificationService notificationService;
    private final AgentConfig agentConfig;
    private final CandidateTools candidateTools;

    @Value("${cloudflare.tunnel.url}")
    private String tunnelUrl;

    @PostMapping(value = "/incoming",
            produces = MediaType.APPLICATION_XML_VALUE)
    public String handleIncomingCall(
            @RequestParam(value = "CallSid",
                    defaultValue = "test") String callSid,
            @RequestParam(value = "From",
                    defaultValue = "unknown") String from) {

        log.info("Incoming call. SID: {}, From: {}", callSid, from);
        callService.createCallLog(callSid, from);

        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Response>
                    <Say voice="Polly.Joanna">Hello. This is Prakash. How can I help you today?</Say>
                    <Gather input="speech"
                            action="%s/api/call/gather"
                            method="POST"
                            speechTimeout="5"
                            timeout="10"
                            language="en-US">
                    </Gather>
                </Response>
                """.formatted(tunnelUrl);
    }

    @PostMapping(value = "/gather",
            produces = MediaType.APPLICATION_XML_VALUE)
    public String handleGather(
            @RequestParam(value = "CallSid",
                    defaultValue = "test") String callSid,
            @RequestParam(value = "SpeechResult",
                    defaultValue = "") String speechResult,
            @RequestParam(value = "From",
                    defaultValue = "unknown") String from) {

        log.info("Speech received. SID: {}, Text: {}",
                callSid, speechResult);

        if (speechResult.isEmpty()) {
            return voiceService.buildNoInputResponse();
        }

        try {
            CallLog callLog = callService.getCallBySid(callSid);
            callService.saveTranscript(callLog, "CALLER", speechResult);

            ConversationAgent agent = agentConfig
                    .getOrCreateConversationAgent(callSid);

            String message = String.format("""
                    Call ID: %s
                    Caller said: %s
                    
                    Detect who is calling and respond
                    in the right mode.
                    Keep response short for phone call.
                    """, callSid, speechResult);

            String aiResponse = agent.chat(message);
            log.info("AI response: {}", aiResponse);

            callService.saveTranscript(callLog, "AI", aiResponse);

            return voiceService.buildSayResponse(aiResponse);

        } catch (Exception e) {
            log.error("Error in gather: {}", e.getMessage());
            return voiceService.buildSayResponse(
                    "Sorry, I had a small issue. " +
                            "Could you please say that again?");
        }
    }

    @PostMapping(value = "/status",
            produces = MediaType.APPLICATION_XML_VALUE)
    public String handleCallStatus(
            @RequestParam(value = "CallSid",
                    defaultValue = "test") String callSid,
            @RequestParam(value = "CallStatus",
                    defaultValue = "") String callStatus) {

        log.info("Call status. SID: {}, Status: {}",
                callSid, callStatus);

        if ("completed".equals(callStatus)) {
            try {
                CallLog callLog = callService.getCallBySid(callSid);

                String transcript = candidateTools
                        .getCallTranscript(callSid);

                if (transcript != null &&
                        !transcript.isEmpty() &&
                        !transcript.equals("No transcript found")) {

                    SummaryAgent summaryAgent = agentConfig
                            .createSummaryAgent();
                    String summary = summaryAgent
                            .summarize(transcript);
                    log.info("Call summary: {}", summary);
                    callService.endCall(callSid, summary);

                    notificationService.sendSmsNotification(
                            callLog, summary);
                } else {
                    callService.endCall(callSid, "No conversation");
                }

                agentConfig.removeAgents(callSid);

            } catch (Exception e) {
                log.error("Error handling call end: {}",
                        e.getMessage());
            }
        }

        return "<Response></Response>";
    }
}