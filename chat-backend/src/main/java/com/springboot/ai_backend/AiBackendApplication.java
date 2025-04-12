package com.springboot.ai_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class
})
@EnableWebFlux
public class AiBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(AiBackendApplication.class, args);
	}
}
