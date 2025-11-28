package com.smartwomen.agents;

import com.smartwomen.models.AgentRequest;
import com.smartwomen.models.AgentResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * LanguageDetectorAgent - Detecta idioma y dialectos LATAM
 * Especializado en 25+ idiomas y dialectos específicos de Latinoamérica
 */
@Component
public class LanguageDetectorAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(LanguageDetectorAgent.class);
    
    @Autowired
    @Qualifier("openAIClient")
    private OpenAIClient openAIClient;
    
    @Value("${spring.azure.cognitive-services.openai.gpt4-deployment}")
    private String gpt4Deployment;
    
    /**
     * Detecta idioma principal y dialecto LATAM específico
     */
    public AgentResponse detectLanguage(AgentRequest request) {
        try {
            String inputText = request.getContent();
            
            // Prompt especializado para detección de dialectos LATAM
            String systemPrompt = """
                Eres un experto en lingüística y idiomas de Latinoamérica.
                Tu tarea es detectar:
                1. Idioma principal
                2. Dialecto/region específica de LATAM
                3. Nivel de formalidad
                4. Contexto cultural relevante
                
                Idiomas y dialectos soportados:
                - Español: México, Colombia, Venezuela, Argentina, Chile, Perú, Ecuador, etc.
                - Portugués: Brasil (distintos estados)
                - Inglés: Con influencia LATAM
                
                Responde SOLO en JSON con esta estructura:
                {
                  "primaryLanguage": "es/pt/en/etc",
                  "dialect": "mexican/colombian/argentinian/etc",
                  "confidence": 0.95,
                  "formality": "formal/informal/mixed",
                  "culturalContext": "descripción breve",
                  "region": "país/región"
                }
                """;
            
            var chatRequest = new com.azure.ai.openai.models.ChatCompletionsOptions(
                List.of(new ChatRequestUserMessage(systemPrompt), 
                       new ChatRequestUserMessage("Texto a analizar: " + inputText))
            )
            .setModel(gpt4Deployment);
            
            var response = openAIClient.getChatCompletions(gpt4Deployment, chatRequest);
            String result = response.getChoices().get(0).getMessage().getContent();
            
            // Parsear JSON de respuesta
            Map<String, Object> parsedResult = parseJsonResponse(result);
            
            logger.info("Language detected: {} / {}", 
                       parsedResult.get("primaryLanguage"), 
                       parsedResult.get("dialect"));
            
            return AgentResponse.builder()
                .agentType("LanguageDetector")
                .content(result)
                .metadata(parsedResult)
                .success(true)
                .processingTime(System.currentTimeMillis() - request.getTimestamp())
                .build();
                
        } catch (Exception e) {
            logger.error("Error detecting language", e);
            return AgentResponse.builder()
                .agentType("LanguageDetector")
                .error("Language detection failed: " + e.getMessage())
                .success(false)
                .build();
        }
    }
    
    private Map<String, Object> parseJsonResponse(String json) {
        Map<String, Object> result = new HashMap<>();
        try {
            // Parse simple JSON (en producción usar Jackson)
            String cleaned = json.replaceAll("[{}]", "").trim();
            String[] pairs = cleaned.split(",");
            
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].replaceAll("\"", "").trim();
                    String value = keyValue[1].replaceAll("\"", "").trim();
                    result.put(key, value);
                }
            }
            
            // Valores por defecto
            if (!result.containsKey("primaryLanguage")) {
                result.put("primaryLanguage", "es");
            }
            if (!result.containsKey("confidence")) {
                result.put("confidence", 0.7);
            }
            
        } catch (Exception e) {
            logger.warn("Could not parse JSON response, using defaults");
            result.put("primaryLanguage", "es");
            result.put("confidence", 0.7);
        }
        return result;
    }
}