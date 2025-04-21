package com.springboot.ai_backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://ai-content-detection-zeta.vercel.app"
})
@RestController
@RequestMapping("/api")
public class AdvisorController {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String SYSTEM_PROMPT = "You are a professional chat advisor specialized in providing concise and helpful advice. Respond in a friendly, conversational tone while maintaining professionalism. Keep responses clear and focused on the user's questions.";

    public AdvisorController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostMapping("/chat")
    public Mono<ResponseEntity<String>> chatWithGemini(@RequestBody Map<String, String> request) {
        String userPrompt = request.get("prompt");

        Map<String, Object> payload = Map.of(
                "contents", List.of(
                        Map.of("role", "user", "parts", List.of(
                                Map.of("text", SYSTEM_PROMPT + "\n\n" + userPrompt)
                        ))
                )
        );

        return webClient.post()
                .uri(geminiApiUrl + "?key=" + geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(60))
                .map(response -> {
                    try {
                        List<?> candidates = (List<?>) response.get("candidates");
                        Map candidate = (Map) candidates.get(0);
                        Map content = (Map) candidate.get("content");
                        List parts = (List) content.get("parts");
                        Map part = (Map) parts.get(0);
                        String result = part.get("text").toString();
                        return ResponseEntity.ok(result);
                    } catch (Exception ex) {
                        return ResponseEntity.internalServerError().body("Invalid response structure from Gemini: " + ex.getMessage());
                    }
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body("Error: " + e.getMessage())));
    }
}
