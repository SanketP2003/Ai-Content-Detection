package com.springboot.ai_backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/detect")
public class AiDetectionController {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(AiDetectionController.class);

    public AiDetectionController(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/bulk-ai")
    public Mono<ResponseEntity<?>> detectContent(@RequestBody Map<String, String> request) {
        final String textToAnalyze = request.get("text");

        if (textToAnalyze == null) {
            return Mono.just(ResponseEntity.badRequest().body(
                    Map.of("error", "Missing required field: text"))
            );
        }

        String[] lines = textToAnalyze.split("\\r?\\n");
        long nonEmptyLines = Arrays.stream(lines)
                .filter(line -> !line.trim().isEmpty())
                .count();

        // Modified validation for 10+ lines
        if (nonEmptyLines < 10) {
            return Mono.just(ResponseEntity.badRequest().body(
                    Map.of("error", "Text must contain at least 10 non-empty lines",
                            "received_lines", nonEmptyLines))
            );
        }

        final String detectionPrompt = createDetectionPrompt(textToAnalyze);

        return webClient.post()
                .uri("http://localhost:11434/api/generate")
                .contentType(MediaType.APPLICATION_JSON) // Add this
                .bodyValue(createOllamaRequest(detectionPrompt))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .flatMap(this::parseDetectionResponse)
                .onErrorResume(this::handleDetectionError);
    }

    private String createDetectionPrompt(String text) {
        return String.format("""
            [INST] Analyze this text for AI generation probability.\s
            Consider these factors:
            1. Perplexity score (0-100)
            2. Burstiness pattern (0-100)
            3. Semantic consistency (0-100)
            4. Known AI patterns
           \s
            Respond ONLY with valid JSON:
            {
                "probability": 0-100,
                "metrics": {
                    "perplexity": number,
                    "burstiness": number,
                    "consistency": number
                },
                "patterns": ["pattern1", ...],
                "analysis": "string"
            }
           \s
            Text: %s [/INST]
           \s""", text);
    }

    private Map<String, Object> createOllamaRequest(String prompt) {
        return Map.of(
                "model", "mistral:instruct",
                "prompt", prompt,
                "options", Map.of(
                        "temperature", 0.1,
                        "num_ctx", 4096,
                        "format", "json"
                ),
                "stream", false
        );
    }
    private Mono<ResponseEntity<?>> parseDetectionResponse(String response) {
        try {
            Map result = objectMapper.readValue(response, Map.class);
            Map<String, Object> responseBody = objectMapper.readValue(
                    (String) result.get("response"),
                    Map.class
            );

            return Mono.just(ResponseEntity.ok(Map.of(
                    "probability", responseBody.get("probability"),
                    "metrics", responseBody.get("metrics"),
                    "patterns", responseBody.get("patterns"),
                    "analysis", responseBody.get("analysis")
            )));
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse detection response: {}", response);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Invalid AI response format")));
        }
    }

    private Mono<ResponseEntity<?>> handleDetectionError(Throwable error) {
        logger.error("AI detection failed: {}", error.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "AI detection service unavailable",
                        "details", error.getMessage()
                )));
    }
}