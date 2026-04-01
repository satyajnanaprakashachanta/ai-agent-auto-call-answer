package com.example.ai_agent_auto_call_answer.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class VoiceService {

    @Value("${cloudflare.tunnel.url}")
    private String tunnelUrl;

    private final String voiceServerUrl = "http://localhost:5050/synthesize";
    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, byte[]> audioCache = new ConcurrentHashMap<>();

    private String preWarmedGreetingToken = null;

    @PostConstruct
    public void preWarmGreeting() {
        log.info("Pre-warming greeting audio...");
        new Thread(() -> {
            String token = generateAndStoreAudio(
                    "Hello. This is Prakash. How can I help you today?"
            );
            if (token != null) {
                preWarmedGreetingToken = token;
                log.info("Greeting audio pre-warmed with token: {}", token);
            }
        }).start();
    }

    public String generateAndStoreAudio(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of("text", text);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    voiceServerUrl,
                    HttpMethod.POST,
                    request,
                    byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String token = UUID.randomUUID().toString();
                audioCache.put(token, response.getBody());
                log.info("Audio stored with token: {}", token);
                return token;
            }
        } catch (Exception e) {
            log.warn("Voice server unavailable: {}", e.getMessage());
        }
        return null;
    }

    public byte[] getAudio(String token) {
        return audioCache.remove(token);
    }

    public String buildGreetingResponse(String candidateName) {
        String token;

        if (preWarmedGreetingToken != null && audioCache.containsKey(preWarmedGreetingToken)) {
            log.info("Using pre-warmed greeting token: {}", preWarmedGreetingToken);
            token = preWarmedGreetingToken;
            preWarmedGreetingToken = null;
            // Pre-warm next greeting in background
            new Thread(() -> {
                String nextToken = generateAndStoreAudio(
                        "Hello. This is Prakash. How can I help you today?"
                );
                if (nextToken != null) {
                    preWarmedGreetingToken = nextToken;
                    log.info("Next greeting pre-warmed: {}", nextToken);
                }
            }).start();
        } else {
            token = generateAndStoreAudio(
                    "Hello. This is Prakash. How can I help you today?"
            );
        }

        if (token != null) {
            return buildPlayResponse(token);
        }
        return buildFallbackGreeting();
    }

    public String buildSayResponse(String message) {
        String clean = message
                .replace("&", "and")
                .replace("<", "")
                .replace(">", "")
                .replace("*", "")
                .replace("#", "")
                .replace("**", "");

        String token = generateAndStoreAudio(clean);
        if (token != null) {
            return buildPlayResponse(token);
        }
        return buildFallbackResponse(clean);
    }

    public String buildEndCallResponse(String message) {
        String text = message + ". Thank you for calling. Have a great day.";
        String token = generateAndStoreAudio(text);

        if (token != null) {
            return """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <Response>
                        <Play>%s/api/audio/%s</Play>
                        <Hangup/>
                    </Response>
                    """.formatted(tunnelUrl, token);
        }
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Response>
                    <Say voice="Polly.Joanna">%s. Thank you for calling.</Say>
                    <Hangup/>
                </Response>
                """.formatted(message);
    }

    public String buildNoInputResponse() {
        String token = generateAndStoreAudio(
                "I am sorry, I did not hear that. Could you please say that again?"
        );
        if (token != null) {
            return buildPlayResponse(token);
        }
        return buildFallbackNoInput();
    }

    private String buildPlayResponse(String token) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Response>
                    <Play>%s/api/audio/%s</Play>
                    <Gather input="speech"
                            action="%s/api/call/gather"
                            method="POST"
                            speechTimeout="5"
                            timeout="10"
                            language="en-US">
                    </Gather>
                </Response>
                """.formatted(tunnelUrl, token, tunnelUrl);
    }

    private String buildFallbackGreeting() {
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

    private String buildFallbackResponse(String message) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Response>
                    <Say voice="Polly.Joanna">%s</Say>
                    <Gather input="speech"
                            action="%s/api/call/gather"
                            method="POST"
                            speechTimeout="5"
                            timeout="10"
                            language="en-US">
                    </Gather>
                </Response>
                """.formatted(message, tunnelUrl);
    }

    private String buildFallbackNoInput() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Response>
                    <Say voice="Polly.Joanna">I am sorry, I did not hear that. Could you please say that again?</Say>
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
}