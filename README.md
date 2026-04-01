# AI Voice Call Agent

An AI-powered agent that automatically answers recruiter calls in my cloned voice, reads my resume, and collects job details.

## Features
- Answers incoming calls automatically via Twilio
- Responds using cloned voice (Coqui TTS XTTS v2)
- Reads resume and answers questions using Groq LLM (llama-3.3-70b)
- Detects caller type: recruiter, friend, family, spam
- Collects job details: company, role, salary, location, sponsorship
- Saves full call transcript to MySQL
- Generates call summary using AI
- Sends SMS notification after call ends

## Tech Stack
- Java 21 + Spring Boot 3
- LangChain4j 0.36.2 (AiServices, memory, tools)
- Groq API (llama-3.3-70b-versatile)
- Twilio Voice API
- Coqui TTS XTTS v2 (voice cloning)
- MySQL + JPA/Hibernate
- Cloudflare Tunnel (public webhook URL)
- Python Flask (voice synthesis server)

## Architecture
```
Recruiter calls Twilio number
       ↓
Twilio sends webhook to Spring Boot
       ↓
ConversationAgent (LangChain4j + Groq)
       ↓
ResumePickerTool selects best resume
       ↓
AI response → Coqui TTS → cloned voice audio
       ↓
Twilio plays audio to caller
       ↓
Transcript saved → Summary generated → SMS sent
```

## Setup

1. Clone the repo
2. Copy `application.properties.example` to `application.properties`
3. Fill in your API keys
4. Set up MySQL database `ai_call_agent`
5. Record your voice sample and convert: `ffmpeg -i voice.wav -ar 22050 -ac 1 voice_22k.wav`
6. Start Python voice server: `python3 voice_server.py`
7. Start Cloudflare tunnel: `cloudflared tunnel --url http://localhost:8080`
8. Run Spring Boot
9. Set Twilio webhook to your tunnel URL

## Author
Prakash Achanta - Software Engineer
