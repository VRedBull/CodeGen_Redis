package com.example.CodeGeneration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiService {

    @Autowired
    private RestTemplate restTemplate;

    private final String GEMINI_API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=%s";
    private final String EXTERNAL_API_URL = "http://localhost:8083/api/codesnippet"; // URL to post the JSON response

    @Cacheable(value = "geminiResponse", key = "#prompt")
    public String callApi(String prompt, String geminiKey) {
        String apiUrl = String.format(GEMINI_API_URL_TEMPLATE, geminiKey);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Prepare the request body for the Gemini API
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode contentNode = objectMapper.createObjectNode();
        ObjectNode partsNode = objectMapper.createObjectNode();
        partsNode.put("text", prompt + "...just the code, no need of explanation");
        contentNode.set("parts", objectMapper.createArrayNode().add(partsNode));
        ObjectNode requestBodyNode = objectMapper.createObjectNode();
        requestBodyNode.set("contents", objectMapper.createArrayNode().add(contentNode));

        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(requestBodyNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct JSON request body", e);
        }

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        // Call the Gemini API and get the response
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
        String responseBody = response.getBody();

        // Parse the response to extract the code, language, and number of lines
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            String code = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            // Extract language (first word after "```")
            String language = code.substring(code.indexOf("```") + 3, code.indexOf("\n")).trim();

            // Extract number of lines in the code (count "\n" in the actual code)
            String codeSnippet = code.substring(code.indexOf("\n") + 1, code.lastIndexOf("```")).trim();
            int numberOfLines = codeSnippet.split("\n").length;

            // Construct the JSON object
            ObjectNode resultJson = objectMapper.createObjectNode();
            resultJson.put("lang", language);
            resultJson.put("nooflines", numberOfLines);
            resultJson.put("code", codeSnippet);

            // Convert to JSON string
            String jsonString = resultJson.toString();

            // Now post this JSON to the external API (localhost:8083/api/codesnippet)
            postToExternalApi(jsonString);

            return jsonString;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response from Gemini API", e);
        }
    }

    private void postToExternalApi(String jsonPayload) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

        try {
            restTemplate.exchange(EXTERNAL_API_URL, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to post to external API", e);
        }
    }
}
