package com.example.ai_agent_auto_call_answer.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface SummaryAgent {

    @SystemMessage("You are summarizing a recruiter call for the candidate to review. Extract: company name, role title, salary range, work location, sponsorship availability, recruiter contact info, and overall impression. Be concise and clear.")
    String summarize(@UserMessage String transcript);
}