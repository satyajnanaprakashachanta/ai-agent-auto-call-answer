package com.example.ai_agent_auto_call_answer.agent;

import com.example.ai_agent_auto_call_answer.tools.CandidateTools;
import com.example.ai_agent_auto_call_answer.tools.ResumePickerTool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AgentConfig {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.base.url}")
    private String groqBaseUrl;

    @Value("${groq.model}")
    private String groqModel;

    private final CandidateTools candidateTools;
    private final ResumePickerTool resumePickerTool;

    private final Map<String, ConversationAgent> conversationAgents
            = new ConcurrentHashMap<>();

    private final Map<String, QuestionCollectorAgent> collectorAgents
            = new ConcurrentHashMap<>();

    private OpenAiChatModel buildModel() {
        return OpenAiChatModel.builder()
                .baseUrl(groqBaseUrl)
                .apiKey(groqApiKey)
                .modelName(groqModel)
                .build();
    }

    public ConversationAgent getOrCreateConversationAgent(String callSid) {
        return conversationAgents.computeIfAbsent(callSid, id -> {
            log.info("Creating ConversationAgent for call: {}", id);
            return AiServices.builder(ConversationAgent.class)
                    .chatLanguageModel(buildModel())
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                    .tools(candidateTools, resumePickerTool)
                    .build();
        });
    }

    public QuestionCollectorAgent getOrCreateCollectorAgent(String callSid) {
        return collectorAgents.computeIfAbsent(callSid, id -> {
            log.info("Creating CollectorAgent for call: {}", id);
            return AiServices.builder(QuestionCollectorAgent.class)
                    .chatLanguageModel(buildModel())
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                    .tools(candidateTools)
                    .build();
        });
    }

    public SummaryAgent createSummaryAgent() {
        return AiServices.builder(SummaryAgent.class)
                .chatLanguageModel(buildModel())
                .build();
    }

    public void removeAgents(String callSid) {
        conversationAgents.remove(callSid);
        collectorAgents.remove(callSid);
        log.info("Agents removed for call: {}", callSid);
    }
}