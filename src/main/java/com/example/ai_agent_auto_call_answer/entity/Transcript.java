package com.example.ai_agent_auto_call_answer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transcript")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Transcript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "call_log_id")
    private CallLog callLog;

    private String speaker;

    @Column(columnDefinition = "TEXT")
    private String messageText;

    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        timestamp = LocalDateTime.now();
    }
}