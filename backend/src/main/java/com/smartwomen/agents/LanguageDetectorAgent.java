package com.smartwomen.agents;

import com.smartwomen.models.AgentRequest;
import com.smartwomen.models.AgentResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.nio.charset.StandardCharsets;

@Component
public class LanguageDetectorAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(LanguageDetectorAgent.class);
    
    private final RestTemplate restTemplate = buildUtf8RestTemplate(); // ✅ RestTemplate UTF-8
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${azure.text-analytics.endpoint}")
    private String textAnalyticsEndpoint;

    @Value("${azure.text-analytics.api-key}")
    private String textAnalyticsApiKey;

    // ✅ Constructor que configura UTF-8
    private RestTemplate buildUtf8RestTemplate() {
        RestTemplate template = new RestTemplate();
        template.getMessageConverters()
            .stream()
            .filter(StringHttpMessageConverter.class::isInstance)
            .forEach(converter -> 
                ((StringHttpMessageConverter) converter).setDefaultCharset(StandardCharsets.UTF_8)
            );
        return template;
    }

    public AgentResponse detectLanguage(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            String inputText = request.getContent();
            logger.info("🌍 INPUT: [{}]", inputText);

            if (inputText == null || inputText.trim().isEmpty()) {
                throw new IllegalArgumentException("Input text is empty");
            }

            String azureResponse = callAzureTextAnalytics(inputText);
            logger.info("📡 AZURE: {}", azureResponse);
            
            Map<String, Object> detection = parseAzureResponse(azureResponse);
            
            return buildSuccessResponse(detection, startTime);
                
        } catch (Exception e) {
            logger.error("❌ FAIL: {}", e.getMessage(), e);
            return buildErrorResponse(e.getMessage(), startTime);
        }
    }
    
    private String callAzureTextAnalytics(String text) {
        String url = textAnalyticsEndpoint + "/text/analytics/v3.1/languages";
        
        try {
            // JSON manual con escaping UTF-8
            String jsonBody = String.format(
                "{\"documents\":[{\"id\":\"1\",\"text\":%s}]}",
                objectMapper.writeValueAsString(text)
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
            headers.set("Ocp-Apim-Subscription-Key", textAnalyticsApiKey);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            logger.debug("📤 SENDING: {}", jsonBody);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Azure HTTP " + response.getStatusCode());
            }
            
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("Azure call error: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private Map<String, Object> parseAzureResponse(String json) {
        Map<String, Object> result = new HashMap<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode documents = root.path("documents");
            
            if (documents.isArray() && documents.size() > 0) {
                JsonNode lang = documents.get(0).path("detectedLanguage");
                result.put("primaryLanguage", lang.path("name").asText("es"));
                result.put("confidence", lang.path("confidenceScore").asDouble(0.7));
                logger.info("✅ DETECTED: {} (confidence: {})", 
                    result.get("primaryLanguage"), 
                    result.get("confidence"));
            } else {
                logger.error("❌ No documents in Azure response");
                result.put("primaryLanguage", "es");
                result.put("confidence", 0.7);
            }
        } catch (Exception e) {
            logger.error("❌ PARSE ERROR: {}", e.getMessage());
            result.put("primaryLanguage", "es");
            result.put("confidence", 0.7);
        }
        return result;
    }
    
    private AgentResponse buildSuccessResponse(Map<String, Object> detection, long startTime) {
        String lang = (String) detection.get("primaryLanguage");
        Double conf = (Double) detection.get("confidence");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("primaryLanguage", lang);
        metadata.put("confidence", conf);
        
        // Enriquecer metadata LATAM
        enrichMetadata(metadata, lang);
        
        return AgentResponse.builder()
                .agentType("LanguageDetector")
                .content(String.format("Detected: %s (%.2f%%)", lang, conf * 100))
                .metadata(metadata)
                .success(true)
                .processingTime(System.currentTimeMillis() - startTime)
                .confidence(conf.toString())
                .build();
    }
    
    private void enrichMetadata(Map<String, Object> metadata, String language) {
        String langLower = language.toLowerCase();
        if (langLower.contains("spanish")) {
            metadata.put("dialect", "latin_american");
            metadata.put("region", "latam_general");
            metadata.put("culturalContext", "Spanish-speaking Latin America");
        } else if (langLower.contains("portuguese")) {
            metadata.put("dialect", "brazilian");
            metadata.put("region", "brazil");
            metadata.put("culturalContext", "Portuguese-speaking Brazil");
        } else {
            metadata.put("dialect", "standard");
            metadata.put("region", "global");
        }
    }
    
    private AgentResponse buildErrorResponse(String error, long startTime) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("primaryLanguage", "es");
        metadata.put("confidence", 0.7);
        
        return AgentResponse.builder()
                .agentType("LanguageDetector")
                .content("Language detection failed")
                .error(error)
                .success(false)
                .processingTime(System.currentTimeMillis() - startTime)
                .confidence("0.7")
                .build();
    }
}