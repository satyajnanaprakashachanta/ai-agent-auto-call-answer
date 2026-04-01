package com.example.ai_agent_auto_call_answer.service;

import com.example.ai_agent_auto_call_answer.entity.CandidateProfile;
import com.example.ai_agent_auto_call_answer.entity.CandidateResume;
import com.example.ai_agent_auto_call_answer.repository.CandidateProfileRepository;
import com.example.ai_agent_auto_call_answer.repository.CandidateResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final CandidateProfileRepository profileRepository;
    private final CandidateResumeRepository resumeRepository;

    public CandidateProfile createProfile(
            String fullName,
            String email,
            String phone,
            String linkedinUrl,
            String githubUrl,
            String salaryMin,
            String salaryMax,
            Boolean needsSponsorship,
            String workPreference,
            String noticePeriod,
            String preferredLocations) {

        CandidateProfile profile = CandidateProfile.builder()
                .fullName(fullName)
                .email(email)
                .personalPhone(phone)
                .linkedinUrl(linkedinUrl)
                .githubUrl(githubUrl)
                .salaryMin(salaryMin)
                .salaryMax(salaryMax)
                .needsSponsorship(needsSponsorship)
                .workPreference(workPreference)
                .noticePeriod(noticePeriod)
                .preferredLocations(preferredLocations)
                .build();

        return profileRepository.save(profile);
    }

    public CandidateResume uploadResume(
            Long candidateId,
            MultipartFile file,
            String resumeType,
            String resumeLabel,
            String keywords,
            Boolean isDefault) throws IOException {

        CandidateProfile candidate = profileRepository
                .findById(candidateId)
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found"));

        // Extract text from PDF using PDFBox
        String resumeText = extractTextFromPdf(file);

        log.info("Resume text extracted. Length: {}",
                resumeText.length());

        CandidateResume resume = CandidateResume.builder()
                .candidate(candidate)
                .resumeType(resumeType)
                .resumeLabel(resumeLabel)
                .keywords(keywords)
                .resumeFileName(file.getOriginalFilename())
                .resumeText(resumeText)
                .isDefault(isDefault)
                .build();

        return resumeRepository.save(resume);
    }

    private String extractTextFromPdf(
            MultipartFile file) throws IOException {

        try (PDDocument document = Loader.loadPDF(
                file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public String getBestResumeForRole(String roleDescription) {

        CandidateProfile candidate = profileRepository
                .findById(1L)
                .orElseThrow(() ->
                        new RuntimeException("No candidate profile found"));

        var allResumes = resumeRepository
                .findByCandidate(candidate);

        String roleLower = roleDescription.toLowerCase();

        // Match keywords
        for (CandidateResume resume : allResumes) {
            String[] keywordList = resume.getKeywords()
                    .toLowerCase().split(",");
            for (String keyword : keywordList) {
                if (roleLower.contains(keyword.trim())) {
                    log.info("Matched resume: {}",
                            resume.getResumeLabel());
                    return resume.getResumeText();
                }
            }
        }

        // Return default if no match
        return resumeRepository
                .findByCandidateAndIsDefaultTrue(candidate)
                .map(CandidateResume::getResumeText)
                .orElse(allResumes.isEmpty() ?
                        "No resume found" :
                        allResumes.get(0).getResumeText());
    }

    public CandidateProfile getProfile() {
        return profileRepository.findById(1L)
                .orElseThrow(() ->
                        new RuntimeException("No candidate profile found"));
    }
}