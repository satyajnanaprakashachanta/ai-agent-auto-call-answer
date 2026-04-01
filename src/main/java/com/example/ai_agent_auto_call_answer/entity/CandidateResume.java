package com.example.ai_agent_auto_call_answer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_resume")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private CandidateProfile candidate;

    private String resumeType;
    private String resumeLabel;

    @Column(columnDefinition = "TEXT")
    private String keywords;

    private String resumeFileName;

    @Column(columnDefinition = "LONGTEXT")
    private String resumeText;

    private Boolean isDefault;
    private LocalDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        uploadedAt = LocalDateTime.now();
        if (isDefault == null) isDefault = false;
    }
}