package com.example.ai_agent_auto_call_answer.controller;

import com.example.ai_agent_auto_call_answer.entity.CandidateProfile;
import com.example.ai_agent_auto_call_answer.entity.CandidateResume;
import com.example.ai_agent_auto_call_answer.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;

    // Create your candidate profile
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createProfile(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String linkedinUrl,
            @RequestParam String githubUrl,
            @RequestParam String salaryMin,
            @RequestParam String salaryMax,
            @RequestParam Boolean needsSponsorship,
            @RequestParam String workPreference,
            @RequestParam String noticePeriod,
            @RequestParam String preferredLocations) {

        CandidateProfile profile = profileService.createProfile(
                fullName, email, phone, linkedinUrl, githubUrl,
                salaryMin, salaryMax, needsSponsorship,
                workPreference, noticePeriod, preferredLocations);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile created successfully");
        response.put("id", profile.getId());
        response.put("name", profile.getFullName());

        return ResponseEntity.ok(response);
    }

    // Upload a resume PDF
    @PostMapping("/resume/upload")
    public ResponseEntity<Map<String, Object>> uploadResume(
            @RequestParam Long candidateId,
            @RequestParam MultipartFile file,
            @RequestParam String resumeType,
            @RequestParam String resumeLabel,
            @RequestParam String keywords,
            @RequestParam Boolean isDefault) {

        try {
            CandidateResume resume = profileService.uploadResume(
                    candidateId, file, resumeType,
                    resumeLabel, keywords, isDefault);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Resume uploaded successfully");
            response.put("resumeId", resume.getId());
            response.put("resumeType", resume.getResumeType());
            response.put("fileName", resume.getResumeFileName());
            response.put("textLength", resume.getResumeText().length());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Resume upload failed: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get current profile
    @GetMapping("/me")
    public ResponseEntity<CandidateProfile> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }

    // Test resume matching
    @GetMapping("/resume/match")
    public ResponseEntity<Map<String, Object>> testMatch(
            @RequestParam String roleDescription) {

        String resumeText = profileService
                .getBestResumeForRole(roleDescription);

        Map<String, Object> response = new HashMap<>();
        response.put("roleDescription", roleDescription);
        response.put("resumePreview",
                resumeText.substring(0, Math.min(200,
                        resumeText.length())) + "...");
        response.put("resumeLength", resumeText.length());

        return ResponseEntity.ok(response);
    }
}