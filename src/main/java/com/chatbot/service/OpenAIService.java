package com.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final String endpoint = "https://api.openai.com/v1/chat/completions";

    public String generateResponse(String userMessage) {
        return generateResponse(userMessage, new ArrayList<>());
    }

    public String generateResponse(String userMessage, List<Map<String, String>> conversationHistory) {
        RestTemplate restTemplate = new RestTemplate(
            new org.springframework.http.client.BufferingClientHttpRequestFactory(
                new org.springframework.http.client.SimpleClientHttpRequestFactory()
            )
        );

        // Build headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Build messages array
        List<Map<String, String>> messages = new ArrayList<>(conversationHistory);
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 150);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                entity,
                String.class
            );

            String body = response.getBody();

            // Check if response is valid JSON before parsing
            if (body == null || !body.trim().startsWith("{")) {
                System.err.println("OpenAI API returned non-JSON response (" + response.getStatusCode() + "): " + body);
                return "Sorry, the API returned an unexpected response. Check your API key.";
            }

            Map<String, Object> responseData = objectMapper.readValue(body, Map.class);

            if (responseData.containsKey("choices") && responseData.get("choices") instanceof List) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseData.get("choices");

                if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }

            // Handle JSON error responses from OpenAI (e.g. invalid API key)
            if (responseData.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) responseData.get("error");
                String errorMsg = (String) error.get("message");
                System.err.println("OpenAI API error: " + errorMsg);
                return "API error: " + errorMsg;
            }

            System.err.println("Error: No valid response from OpenAI API: " + body);
            return "Sorry, I couldn't understand that.";

        } catch (HttpClientErrorException e) {
            System.err.println("OpenAI API error (" + e.getStatusCode() + "): " + e.getResponseBodyAsString());
            return "Sorry, there was an API error: " + e.getStatusCode();
        } catch (Exception e) {
            System.err.println("Error calling OpenAI API: " + e.getMessage());
            e.printStackTrace();
            return "Sorry, I couldn't understand that.";
        }
    }
}
