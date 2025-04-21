package com.springboot.ai_backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class AdvisorController {

    private final WebClient webClient;

    @Value("${llama.api.url}")
    private String llamaApiUrl;

    @Value("${llama.api.key}")
    private String llamaApiKey;

    @Value("${llama.model}")
    private String model;

    @Value("${llama.temperature}")
    private double temperature;

    @Value("${llama.max-tokens}")
    private int maxTokens;

    private static final String SYSTEM_PROMPT = "You are a professional chat advisor specialized in providing concise and helpful advice. Respond in a friendly, conversational tone while maintaining professionalism. Keep responses clear and focused on the user's questions.";

    public AdvisorController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostMapping("/chat")
    public Mono<ResponseEntity<String>> chatWithLlama(@RequestBody Map<String, String> request) {
        String userPrompt = request.get("prompt");

        Map<String, Object> payload = Map.of(
                "model", model,
                "temperature", temperature,
                "max_tokens", maxTokens,
                "messages", new Object[]{
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userPrompt)
                }
        );

        return webClient.post()
                .uri(llamaApiUrl)
                .header("Authorization", "Bearer " + llamaApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(15))
                .map(response -> {
                    String content = ((Map)((Map)((java.util.List) response.get("choices")).get(0)).get("message")).get("content").toString();
                    return ResponseEntity.ok(content);
                })
                .onErrorResume(e -> {
                    return Mono.just(ResponseEntity.internalServerError().body("Error: " + e.getMessage()));
                });
    }
}
