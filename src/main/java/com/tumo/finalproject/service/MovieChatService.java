package com.tumo.finalproject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MovieChatService {

    private static final String MODEL = "llama-3.3-70b-versatile";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // Record definition using java.util.List
    public record ChatResult(String reply, List<String> titles) {
    }

    public MovieChatService(@Value("${groq.api.key:}") String apiKey) {
        this.objectMapper = new ObjectMapper();

        String cleanKey = apiKey != null ? apiKey.trim().replace("\"", "") : "";

        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Authorization", "Bearer " + cleanKey)
                .build();
    }

    public ChatResult chat(String userMessage) {
        String systemPrompt = "You are a friendly movie recommendation assistant. Recommend movies based on user preferences. " +
                "Respond ONLY with a JSON object shaped as: {\"reply\": string, \"movies\": [{\"title\": string, \"year\": string}]}.";

        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "response_format", Map.of("type", "json_object")
        );

        try {
            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractResult(response);
        } catch (Exception e) {
            return new ChatResult("Unable to reach AI service. Please verify your GROQ API key in application.properties.", List.of());
        }
    }

    private ChatResult extractResult(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode choices = root.get("choices");

            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                return new ChatResult("No response was returned by the AI.", List.of());
            }

            String content = choices.get(0).get("message").get("content").asText();
            JsonNode parsed = objectMapper.readTree(content);

            String reply = parsed.has("reply") ? parsed.get("reply").asText() : content;
            List<String> titles = new ArrayList<>();

            if (parsed.has("movies") && parsed.get("movies").isArray()) {
                for (JsonNode movieNode : parsed.get("movies")) {
                    if (movieNode.has("title")) {
                        titles.add(movieNode.get("title").asText());
                    }
                }
            }

            return new ChatResult(reply, titles);
        } catch (Exception e) {
            return new ChatResult("Failed to process recommendation format.", List.of());
        }
    }
}