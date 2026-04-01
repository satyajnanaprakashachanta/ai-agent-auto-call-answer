package com.example.ai_agent_auto_call_answer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String callSid;
    private String recruiterPhone;
    private String recruiterName;
    private String companyName;
    private String roleTitle;
    private String salaryRange;
    private String workLocation;
    private Boolean sponsorshipAvailable;
    private String workType;
    private Integer callDurationSeconds;
    private String callStatus;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private Boolean summarySent;
    private LocalDateTime calledAt;
    private LocalDateTime endedAt;

    @PrePersist
    public void prePersist() {
        calledAt = LocalDateTime.now();
        summarySent = false;
        callStatus = "ongoing";
    }
}