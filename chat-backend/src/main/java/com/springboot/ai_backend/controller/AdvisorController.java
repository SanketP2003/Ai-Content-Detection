package com.springboot.ai_backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@CrossOrigin(origins = "https://ai-content-detection-dun.vercel.app/dashboard")
@RestController
@RequestMapping("/api")
public class AdvisorController {

    private final WebClient webClient;
    private static final String SYSTEM_PROMPT = "You are a professional chat advisor specialized in providing " +
            "concise and helpful advice. Respond in a friendly, conversational tone while maintaining " +
            "professionalism. Keep responses clear and focused on the user's questions.";

    @Autowired
    public AdvisorController(WebClient webClient) {
        this.webClient = webClient;
    }

    @PostMapping("/chat")
    public Mono<ResponseEntity<String>> chatWithAdvisor(@RequestBody Map<String, String> request) {
        return webClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "model", "mistral:7b",
                        "prompt", request.get("prompt"),
                        "system", SYSTEM_PROMPT,
                        "options", Map.of(
                                "temperature", 0.3,
                                "max_tokens", 300
                        ),
                        "stream", false
                ))
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> ResponseEntity.ok().body(parseResponse(response)))
                .timeout(Duration.ofSeconds(10))
                .onErrorReturn(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                        .body("Our chat advisor is currently busy. Please try again shortly."));
    }

    private String parseResponse(String json) {
        try {
            JsonNode node = new ObjectMapper().readTree(json);
            return node.path("response").asText();
        } catch (Exception e) {
            return "Could not process the advisor's response. Please try again.";
        }
    }
}