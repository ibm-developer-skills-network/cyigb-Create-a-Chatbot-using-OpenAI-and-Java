package com.chatbot.controller;

import com.chatbot.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ChatbotController {

    @Autowired
    private OpenAIService openAIService;

    @PostMapping("/getChatbotResponse")
    public Map<String, String> getChatbotResponse(@RequestBody Map<String, String> request) {
        String userMessage = request.get("userMessage");
        
        // Use OpenAI API to generate a response
        String chatbotResponse = openAIService.generateResponse(userMessage);
        
        // Send the response back to the client
        Map<String, String> response = new HashMap<>();
        response.put("chatbotResponse", chatbotResponse);
        return response;
    }
}
