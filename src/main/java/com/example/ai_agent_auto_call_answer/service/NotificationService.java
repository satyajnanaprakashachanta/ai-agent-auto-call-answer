package com.example.ai_agent_auto_call_answer.service;

import com.example.ai_agent_auto_call_answer.entity.CallLog;
import com.example.ai_agent_auto_call_answer.entity.NotificationLog;
import com.example.ai_agent_auto_call_answer.repository.NotificationLogRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhone;

    @Value("${your.phone.number}")
    private String yourPhone;

    public void sendSmsNotification(
            CallLog callLog, String summary) {

        try {
            Twilio.init(accountSid, authToken);

            String message = """
                    NEW RECRUITER CALL SUMMARY
                    --------------------------
                    Company: %s
                    Role: %s
                    Salary: %s
                    Sponsorship: %s
                    Location: %s
                    Recruiter Phone: %s
                    --------------------------
                    %s
                    """.formatted(
                    callLog.getCompanyName(),
                    callLog.getRoleTitle(),
                    callLog.getSalaryRange(),
                    callLog.getSponsorshipAvailable(),
                    callLog.getWorkLocation(),
                    callLog.getRecruiterPhone(),
                    summary
            );

            Message.creator(
                    new PhoneNumber(yourPhone),
                    new PhoneNumber(twilioPhone),
                    message
            ).create();

            log.info("SMS notification sent successfully");

            // Save to notification log
            NotificationLog notifLog = NotificationLog.builder()
                    .callLog(callLog)
                    .channel("sms")
                    .messageSent(message)
                    .status("sent")
                    .build();

            notificationLogRepository.save(notifLog);

        } catch (Exception e) {
            log.error("Failed to send SMS: {}", e.getMessage());

            NotificationLog notifLog = NotificationLog.builder()
                    .callLog(callLog)
                    .channel("sms")
                    .messageSent(summary)
                    .status("failed")
                    .build();

            notificationLogRepository.save(notifLog);
        }
    }
}