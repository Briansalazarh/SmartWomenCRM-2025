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
public class SentimentAnalyzerAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(SentimentAnalyzerAgent.class);
    
    private final RestTemplate restTemplate = buildUtf8RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${azure.text-analytics.endpoint}")
    private String textAnalyticsEndpoint;

    @Value("${azure.text-analytics.api-key}")
    private String textAnalyticsApiKey;

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

    public AgentResponse analyzeSentiment(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            String inputText = request.getContent();
            logger.info("🌍 INPUT: [{}]", inputText);

            // Llamar a Azure Text Analytics (sentiment)
            String azureResponse = callAzureTextAnalytics(inputText);
            logger.info("📡 AZURE RESPONSE: {}", azureResponse);
            
            // Parsear respuesta
            JsonNode root = objectMapper.readTree(azureResponse);
            JsonNode documents = root.path("documents");
            
            if (!documents.isArray() || documents.size() == 0) {
                throw new RuntimeException("No documents in Azure response");
            }
            
            JsonNode document = documents.get(0);
            String sentiment = document.path("sentiment").asText("neutral");
            JsonNode confidenceScores = document.path("confidenceScores");
            
            // Crear resultado estructurado
            Map<String, Object> sentimentResult = new HashMap<>();
            sentimentResult.put("sentiment", sentiment);
            sentimentResult.put("confidence", confidenceScores.path(sentiment).asDouble(0.0));
            sentimentResult.put("primaryEmotion", mapSentimentToEmotion(sentiment));
            sentimentResult.put("requiresEscalation", sentiment.equals("negative") && confidenceScores.path("negative").asDouble(0.0) > 0.7);
            
            // Enriquecer con contexto LATAM
            Map<String, Object> metadata = enrichLatamMetadata(sentiment, confidenceScores);
            
            return AgentResponse.builder()
                .agentType("SentimentAnalyzer")
                .content(objectMapper.writeValueAsString(sentimentResult))
                .metadata(metadata)
                .success(true)
                .processingTime(System.currentTimeMillis() - startTime)
                .confidence(String.valueOf(confidenceScores.path(sentiment).asDouble(0.0)))
                .build();
                
        } catch (Exception e) {
            logger.error("❌ ANALYSIS FAILED", e);
            return buildErrorResponse(e.getMessage(), startTime);
        }
    }
    
    private String callAzureTextAnalytics(String text) {
        String url = textAnalyticsEndpoint + "/text/analytics/v3.1/sentiment";
        
        try {
            String jsonBody = String.format(
                "{\"documents\":[{\"id\":\"1\",\"text\":%s}]}",
                objectMapper.writeValueAsString(text)
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
            headers.set("Ocp-Apim-Subscription-Key", textAnalyticsApiKey);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Azure HTTP " + response.getStatusCode());
            }
            
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("❌ AZURE CALL FAILED: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private String mapSentimentToEmotion(String sentiment) {
        switch (sentiment) {
            case "positive": return "joy";
            case "negative": return "anger";
            case "neutral": return "neutral";
            default: return "mixed";
        }
    }
    
    private Map<String, Object> enrichLatamMetadata(String sentiment, JsonNode confidenceScores) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("azureSentiment", sentiment);
        metadata.put("azureConfidence", confidenceScores);
        metadata.put("culturalContext", "Análisis adaptado para contexto LATAM");
        metadata.put("requiresEscalation", sentiment.equals("negative") && confidenceScores.path("negative").asDouble(0.0) > 0.7);
        return metadata;
    }
    
    private AgentResponse buildErrorResponse(String error, long startTime) {
        return AgentResponse.builder()
                .agentType("SentimentAnalyzer")
                .content("Analysis failed")
                .error(error)
                .success(false)
                .processingTime(System.currentTimeMillis() - startTime)
                .confidence("0.0")
                .build();
    }
}