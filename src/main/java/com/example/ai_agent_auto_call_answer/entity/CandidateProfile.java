package com.example.ai_agent_auto_call_answer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "candidate_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String personalPhone;
    private String linkedinUrl;
    private String githubUrl;
    private String salaryMin;
    private String salaryMax;
    private Boolean needsSponsorship;
    private String workPreference;
    private String noticePeriod;
    private String preferredLocations;

    @OneToMany(mappedBy = "candidate",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<CandidateResume> resumes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}