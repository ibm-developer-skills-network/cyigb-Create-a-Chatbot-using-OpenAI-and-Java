package com.chatbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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
        RestTemplate restTemplate = new RestTemplate();

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
        requestBody.put("model", "gpt-3.5-turbo-1106");
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 150);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Make API call
            ResponseEntity<Map> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                entity,
                Map.class
            );

            Map<String, Object> responseData = response.getBody();

            // Log the entire API response for debugging
            if (responseData != null && responseData.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseData.get("choices");
                if (!choices.isEmpty()) {
                    System.out.println("Response from OpenAI API: " + choices.get(0).get("message"));
                }
            }

            // Check if choices array is defined and not empty
            if (responseData != null && 
                responseData.containsKey("choices") && 
                responseData.get("choices") instanceof List) {
                
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseData.get("choices");
                
                if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }

            // Handle the case where choices array is undefined or empty
            System.err.println("Error: No valid response from OpenAI API");
            return "Sorry, I couldn't understand that.";

        } catch (Exception e) {
            System.err.println("Error calling OpenAI API: " + e.getMessage());
            e.printStackTrace();
            return "Sorry, I couldn't understand that.";
        }
    }
}
