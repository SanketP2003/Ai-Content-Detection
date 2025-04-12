package com.springboot.ai_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebFilter;

@Configuration
public class WebConfig {
    @Bean
    public WebFilter corsFilter() {
        return (exchange, chain) -> {
            exchange.getResponse().getHeaders()
                    .add("Access-Control-Allow-Origin", "*");
            exchange.getResponse().getHeaders()
                    .add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponse().getHeaders()
                    .add("Access-Control-Allow-Headers", "Content-Type");
            return chain.filter(exchange);
        };
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }
}
