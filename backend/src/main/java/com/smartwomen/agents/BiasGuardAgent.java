package com.smartwomen.agents;

import com.smartwomen.models.AgentRequest;
import com.smartwomen.models.AgentResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.contentsafety.ContentSafetyClient;
import com.azure.ai.contentsafety.models.AnalyzeTextOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.nio.charset.StandardCharsets;

@Component
public class BiasGuardAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(BiasGuardAgent.class);
    
    @Autowired
    @Qualifier("openAIClient")
    private OpenAIClient openAIClient;
    
    @Autowired
    @Qualifier("contentSafetyClient")
    private ContentSafetyClient contentSafetyClient;
    
    @Value("${azure.openai.gpt4-deployment}")
    private String gpt4Deployment;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Detecta y analiza sesgos en el contenido
     */
    public AgentResponse detectBias(AgentRequest request) {
        try {
            String inputText = request.getContent();
            logger.info("🌍 INPUT: [{}]", inputText);
            
            // Análisis Azure Content Safety - USANDO SDK
            AnalyzeTextOptions analyzeTextRequest = new AnalyzeTextOptions(inputText);
            var azureSafetyResult = contentSafetyClient.analyzeText(analyzeTextRequest);
            logger.info("📡 AZURE CONTENT SAFETY: {}", azureSafetyResult);
            
            // Análisis contextual con GPT-4 especializado en sesgos
            String systemPrompt = buildBiasPrompt(azureSafetyResult.toString(), inputText);
            
            var chatRequest = new com.azure.ai.openai.models.ChatCompletionsOptions(
                List.of(
                    new com.azure.ai.openai.models.ChatRequestSystemMessage(systemPrompt),
                    new com.azure.ai.openai.models.ChatRequestUserMessage("Texto a analizar: " + inputText)
                )
            ).setModel(gpt4Deployment);
            
            var response = openAIClient.getChatCompletions(gpt4Deployment, chatRequest);
            String aiAnalysis = response.getChoices().get(0).getMessage().getContent();
            logger.info("🤖 GPT-4 ANALYSIS: {}", aiAnalysis);
            
            // Parsear y estructurar resultado con formato limpio
            Map<String, Object> biasResult = parseBiasAnalysis(aiAnalysis);
            
            // Metadata limpia y correcta
            Map<String, Object> metadata = buildMetadata(biasResult, azureSafetyResult);
            
            // Determinar acción final
            String finalAction = determineAction(biasResult);
            metadata.put("finalAction", finalAction);
            
            logger.info("✅ BIAS DETECTED: {}, SEVERITY: {}, ACTION: {}", 
                       biasResult.get("biasDetected"),
                       biasResult.get("severity"),
                       finalAction);
            
            return AgentResponse.builder()
                .agentType("BiasGuard")
                .content(objectMapper.writeValueAsString(biasResult))  // ✅ JSON parseable
                .metadata(metadata)
                .success(true)
                .processingTime(System.currentTimeMillis() - request.getTimestamp())
                .confidence(String.valueOf(biasResult.get("confidence")))  // ✅ Confidence extraído
                .build();
                
        } catch (Exception e) {
            logger.error("❌ BIAS DETECTION FAILED", e);
            return buildErrorResponse(e.getMessage(), request.getTimestamp());
        }
    }
    
    private String buildBiasPrompt(String azureResult, String inputText) {
        return """
            Eres un experto en detección de sesgos y discriminación.
            Analiza el texto para detectar sesgos de género, culturales y socioeconómicos.
            Considera el contexto LATAM y perspectiva de género.
            
            ANÁLISIS AZURE CONTENT SAFETY:
            """ + azureResult + """
            
            TEXTO A ANALIZAR:
            """ + inputText + """
            
            Responde SOLO en JSON con estos campos:
            {
              "biasDetected": true/false,
              "biasTypes": ["gender", "cultural"],
              "severity": 0.0-1.0,
              "confidence": 0.0-1.0,
              "affectedGroups": ["mujeres", "jóvenes"],
              "action": "allow/warn/block/escalate",
              "reason": "explicación detallada",
              "alternativeSuggestion": "texto alternativo"
            }
            """;
    }
    
    private Map<String, Object> parseBiasAnalysis(String aiAnalysis) {
        try {
            // Limpiar si viene con ```json o ```
            String cleaned = aiAnalysis.replaceAll("```json", "").replaceAll("```", "").trim();
            return objectMapper.readValue(cleaned, Map.class);
        } catch (Exception e) {
            logger.warn("❌ Could not parse GPT-4 JSON: {}", e.getMessage());
            Map<String, Object> defaults = new HashMap<>();
            defaults.put("biasDetected", false);
            defaults.put("severity", 0.0);
            defaults.put("confidence", 0.5);
            defaults.put("action", "allow");
            defaults.put("reason", "Parse error: " + e.getMessage());
            defaults.put("alternativeSuggestion", "Revisar manualmente");
            return defaults;
        }
    }
    
    private Map<String, Object> buildMetadata(Map<String, Object> biasResult, Object azureResult) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("biasDetected", biasResult.get("biasDetected"));
        metadata.put("biasTypes", biasResult.get("biasTypes"));
        metadata.put("severity", biasResult.get("severity"));
        metadata.put("confidence", biasResult.get("confidence"));
        metadata.put("affectedGroups", biasResult.get("affectedGroups"));
        metadata.put("azureAnalysis", "Content Safety Analysis Complete");
        metadata.put("gpt4Analysis", "Bias analysis completed");
        return metadata;
    }
    
    private String determineAction(Map<String, Object> result) {
        Boolean detected = (Boolean) result.get("biasDetected");
        Double severity = (Double) result.get("severity");
        
        if (detected != null && detected && severity != null) {
            if (severity >= 0.8) return "escalate";
            if (severity >= 0.6) return "block";
            if (severity >= 0.3) return "warn";
        }
        return "allow";
    }
    
    private AgentResponse buildErrorResponse(String error, long timestamp) {
        return AgentResponse.builder()
                .agentType("BiasGuard")
                .content("Bias detection failed")
                .error(error)
                .success(false)
                .processingTime(System.currentTimeMillis() - timestamp)
                .confidence("0.0")
                .build();
    }
}