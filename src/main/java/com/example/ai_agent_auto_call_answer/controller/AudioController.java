package com.example.ai_agent_auto_call_answer.controller;

import com.example.ai_agent_auto_call_answer.service.VoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
@Slf4j
public class AudioController {

    private final VoiceService voiceService;

    @GetMapping("/{token}")
    public ResponseEntity<byte[]> serveAudio(@PathVariable String token) {
        byte[] audio = voiceService.getAudio(token);

        if (audio == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/wav"));
        headers.setContentLength(audio.length);

        log.info("Serving audio for token: {}", token);
        return ResponseEntity.ok().headers(headers).body(audio);
    }
}
