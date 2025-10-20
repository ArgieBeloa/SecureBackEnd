package com.example.ThesisBackend.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpoNotificationService {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    public void sendPushNotification(List<String> expoTokens, String title, String body) {
        RestTemplate restTemplate = new RestTemplate();

        for (String token : expoTokens) {
            if (token == null || token.isEmpty()) continue;

            Map<String, Object> payload = new HashMap<>();
            payload.put("to", token);
            payload.put("sound", "default");
            payload.put("title", title);
            payload.put("body", body);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            try {
                ResponseEntity<String> response = restTemplate.postForEntity(EXPO_PUSH_URL, request, String.class);
                System.out.println("📨 Sent push to " + token + ": " + response.getBody());
            } catch (Exception e) {
                System.out.println("❌ Failed to send push to " + token + ": " + e.getMessage());
            }
        }
    }
}
