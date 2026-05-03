package org.java.assesment.quiz_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.java.assesment.quiz_service.dto.question.BatchImportRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiQuestionService {

    @Value("${app.claude.api-key}")
    private String apiKey;

    @Value("${app.claude.model}")
    private String model;

    @Value("${app.claude.max-tokens}")
    private int maxTokens;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Force JDK HTTP client — avoids Netty/Reactor classpath conflicts
    private final RestClient restClient = RestClient.builder()
            .requestFactory(new JdkClientHttpRequestFactory(
                    HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(30))
                            .build()))
            .build();

    /**
     * Calls Claude API and returns a list of parsed question import items
     * ready to be handed off to QuestionService.batchImport().
     */
    public List<BatchImportRequest.QuestionImportItem> generateQuestions(
            String topic, String javaVersion, int count) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("CLAUDE_API_KEY is not configured.");
        }

        String prompt = buildPrompt(topic, javaVersion, count);
        String raw = callClaude(prompt);
        return parseItems(raw);
    }

    // ── Claude HTTP call ─────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String callClaude(String userPrompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "system", """
                        You are an expert Java certification exam author.
                        You ALWAYS return ONLY a raw JSON array — no markdown fences, no commentary, no extra text.
                        Every JSON string value that contains Java code MUST use proper escaped newlines (\\n) inside the JSON string.
                        """,
                "messages", List.of(
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        Map<String, Object> response = restClient.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        // Extract text from response.content[0].text
        var contentList = (List<Map<String, Object>>) response.get("content");
        return (String) contentList.get(0).get("text");
    }

    // ── Prompt ───────────────────────────────────────────────────────────────

    private String buildPrompt(String topic, String javaVersion, int count) {
        return """
                Generate %d Java interview/certification questions about "%s" for Java %s developers.

                Requirements:
                - Mix RADIO (single correct answer) and CHECKBOX (multiple correct answers) types
                - Include Java code examples using fenced blocks where helpful: ```java\\n...\\n```
                - Every question must have 3-5 answer options
                - Include a question-level explanation shown after exam submission
                - Focus on real-world gotchas, edge cases, and Java-version-specific features
                - All questions start as BETA status

                Return ONLY a valid JSON array with this exact schema:
                [
                  {
                    "questionText": "Question text. Code block example:\\n```java\\nString s = null;\\nSystem.out.println(s.length());\\n```\\nWhat happens?",
                    "explanation": "Full explanation shown to user after submitting the exam.",
                    "answerType": "RADIO",
                    "status": "BETA",
                    "answers": [
                      { "text": "Answer option A", "correct": true,  "explanation": "Why A is correct", "orderIndex": 0 },
                      { "text": "Answer option B", "correct": false, "explanation": "Why B is wrong",   "orderIndex": 1 }
                    ]
                  }
                ]
                """.formatted(count, topic, javaVersion);
    }

    // ── JSON parsing ─────────────────────────────────────────────────────────

    private List<BatchImportRequest.QuestionImportItem> parseItems(String raw) {
        try {
            // Strip markdown fences if model added them despite instructions
            String cleaned = raw.strip();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").strip();
            }
            return objectMapper.readValue(cleaned,
                    new TypeReference<List<BatchImportRequest.QuestionImportItem>>() {});
        } catch (Exception e) {
            log.error("Failed to parse Claude response: {}", raw, e);
            throw new IllegalStateException("AI returned unparseable response. Try again.");
        }
    }
}
