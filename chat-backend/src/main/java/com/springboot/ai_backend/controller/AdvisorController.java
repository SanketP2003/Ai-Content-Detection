package com.springboot.ai_backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

    @Value("${ai.model.api.url}")
    private String modelApiUrl;
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
        String prompt = request.get("prompt");

        System.out.println("Received prompt: " + prompt); // 🔍 Log for backend verification

        String mockResponse = "🤖 Mocked response to: " + prompt;

        return Mono.just(ResponseEntity.ok(mockResponse));
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