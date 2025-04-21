package com.springboot.ai_backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/detect")
public class AiDetectionController {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
        Analyze text for AI generation probability. Respond with JSON containing:
        {
            "probability": 0-100,
            "metrics": {
                "perplexity": 0-100,
                "burstiness": 0-100,
                "consistency": 0-100
            },
            "patterns": ["pattern1", ...],
            "analysis": "string"
        }
        """;

    public AiDetectionController(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${llama.api.url}") String llamaApiUrl,
            @Value("${llama.api.key}") String llamaApiKey
    ) {
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder
                .baseUrl(llamaApiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + llamaApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @PostMapping("/bulk-ai")
    public Mono<? extends ResponseEntity<?>> detectContent(@RequestBody Map<String, String> request) {
        String textToAnalyze = request.get("text");

        if (textToAnalyze == null || textToAnalyze.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(Map.of("error", "Text content is required")));
        }

        // NVIDIA's LLaMA format via NIM
        Map<String, Object> body = Map.of(
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", textToAnalyze)
                )
        );

        return webClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .flatMap(json -> {
                    try {
                        // NVIDIA response: usually nested like choices[0].message.content
                        Map<String, Object> map = objectMapper.readValue(json, new TypeReference<>() {});
                        Map<String, Object> choice = (Map<String, Object>) ((List<?>) map.get("choices")).get(0);
                        Map<String, Object> message = (Map<String, Object>) choice.get("message");
                        String content = (String) message.get("content");

                        Map<String, Object> aiAnalysis = objectMapper.readValue(content, new TypeReference<>() {});
                        return Mono.just(ResponseEntity.ok(aiAnalysis));

                    } catch (Exception e) {
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Failed to parse LLaMA response", "details", e.getMessage())));
                    }
                })
                .onErrorResume(err ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "LLaMA request failed", "details", err.getMessage())))
                );
    }
}
