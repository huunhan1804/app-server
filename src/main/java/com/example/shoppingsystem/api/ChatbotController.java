package com.example.shoppingsystem.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatbotController {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");

        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> body = Map.of(
                    "model", "gpt-3.5-turbo",
                    "messages", new Object[]{
                            Map.of("role", "user", "content", message)
                    }
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions",
                    new org.springframework.http.HttpEntity<>(body, getHeaders()),
                    Map.class
            );

            Map data = response.getBody();
            String reply = (String) ((Map)((Map)((java.util.List) data.get("choices")).get(0)).get("message")).get("content");

            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private org.springframework.http.HttpHeaders getHeaders() {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiApiKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }
}