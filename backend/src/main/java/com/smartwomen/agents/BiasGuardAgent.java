package com.smartwomen.agents;

import com.smartwomen.models.AgentRequest;
import com.smartwomen.models.AgentResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.contentsafety.ContentSafetyClient;
import com.azure.ai.contentsafety.models.TextCategory;
import com.azure.ai.contentsafety.models.TextBlocklistMatch;
import com.azure.ai.contentsafety.models.AnalyzeTextOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * BiasGuardAgent - Sistema anti-sesgo automático
 * Detecta y bloquea/modera contenido con estereotipos de género, culturales y socioeconómicos
 */
@Component
public class BiasGuardAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(BiasGuardAgent.class);
    
    @Autowired
    @Qualifier("openAIClient")
    private OpenAIClient openAIClient;
    
    @Autowired
    @Qualifier("contentSafetyClient")
    private ContentSafetyClient contentSafetyClient;
    
    @Value("${spring.azure.cognitive-services.openai.gpt4-deployment}")
    private String gpt4Deployment;
    
    // Tipos de sesgos a detectar
    private static final List<String> BIAS_TYPES = List.of(
        "gender", "cultural", "socioeconomic", "racial", "professional", "age", "appearance"
    );
    
    /**
     * Detecta y analiza sesgos en el contenido
     */
    public AgentResponse detectBias(AgentRequest request) {
        try {
            String inputText = request.getContent();
            
            // Análisis Azure Content Safety - Configuración correcta
            var analyzeTextRequest = new com.azure.ai.contentsafety.models.AnalyzeTextOptions(inputText);
            var azureSafetyResult = contentSafetyClient.analyzeText(analyzeTextRequest);
            
            // Análisis contextual con GPT-4 especializado en sesgos
            String systemPrompt = """
                Eres un experto en detección de sesgos y discriminación.
                Analiza el texto para detectar:
                
                1. SESGOS DE GÉNERO: 
                   - Estereotipos sobre capacidades femeninas
                   - Roles tradicionales vs. empoderamiento
                   - Lenguaje condescendiente hacia mujeres
                
                2. SESGOS CULTURALES LATAM:
                   - Generalizaciones sobre países latinoamericanos
                   - Prejuicios sobre economía regional
                   - Estereotipos culturales
                
                3. SESGOS SOCIOECONÓMICOS:
                   - Discriminación por clase social
                   - Supuestos sobre poder adquisitivo
                
                4. SEVERIDAD (0.0-1.0):
                   - 0.0-0.3: Bajo (educativo)
                   - 0.3-0.6: Medio (requiere corrección)
                   - 0.6-0.8: Alto (bloquear contenido)
                   - 0.8-1.0: Crítico (escalación inmediata)
                
                ANÁLISIS AZURE CONTENT SAFETY:
                """ + azureSafetyResult.toString() + """
                
                Responde SOLO en JSON:
                {
                  "biasDetected": true/false,
                  "biasTypes": ["gender", "cultural", etc],
                  "severity": 0.0-1.0,
                  "confidence": 0.0-1.0,
                  "affectedGroups": ["mujeres", "latinos", etc],
                  "action": "allow/warn/block/escalate",
                  "reason": "explicación detallada",
                  "alternativeSuggestion": "propuesta de contenido alternativo"
                }
                """;
            
            var chatRequest = new com.azure.ai.openai.models.ChatCompletionsOptions(
                List.of(new ChatRequestUserMessage(systemPrompt), 
                       new ChatRequestUserMessage("Texto a analizar: " + inputText))
            ).setModel(gpt4Deployment);
            
            var response = openAIClient.getChatCompletions(gpt4Deployment, chatRequest);
            String aiAnalysis = response.getChoices().get(0).getMessage().getContent();
            
            Map<String, Object> parsedResult = parseBiasJson(aiAnalysis);
            Map<String, Object> metadata = new HashMap<>();
            
            // Combinar análisis Azure + contextual
            metadata.putAll(parsedResult);
            metadata.put("azureAnalysis", azureSafetyResult.toString());
            
            // Determinar acción final
            String finalAction = determineAction(parsedResult);
            metadata.put("finalAction", finalAction);
            
            logger.info("Bias analysis - Detected: {}, Severity: {}, Action: {}", 
                       parsedResult.get("biasDetected"),
                       parsedResult.get("severity"),
                       finalAction);
            
            return AgentResponse.builder()
                .agentType("BiasGuard")
                .content(aiAnalysis)
                .metadata(metadata)
                .success(true)
                .processingTime(System.currentTimeMillis() - request.getTimestamp())
                .build();
                
        } catch (Exception e) {
            logger.error("Error detecting bias", e);
            return AgentResponse.builder()
                .agentType("BiasGuard")
                .error("Bias detection failed: " + e.getMessage())
                .success(false)
                .build();
        }
    }
    
    /**
     * Determina la acción a tomar basada en la severidad del sesgo
     */
    private String determineAction(Map<String, Object> result) {
        if (Boolean.TRUE.equals(result.get("biasDetected"))) {
            Double severity = (Double) result.get("severity");
            if (severity != null) {
                if (severity >= 0.8) return "escalate";
                if (severity >= 0.6) return "block";
                if (severity >= 0.3) return "warn";
            }
        }
        return "allow";
    }
    
    private Map<String, Object> parseBiasJson(String json) {
        Map<String, Object> result = new HashMap<>();
        try {
            String cleaned = json.replaceAll("[{}]", "").trim();
            String[] pairs = cleaned.split(",");
            
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length >= 2) {
                    String key = keyValue[0].replaceAll("\"", "").trim();
                    String value = keyValue[1].trim();
                    
                    // Valores booleanos
                    if (key.equals("biasDetected")) {
                        result.put(key, Boolean.parseBoolean(value));
                    }
                    // Valores numéricos
                    else if (key.equals("severity") || key.equals("confidence")) {
                        try {
                            result.put(key, Double.parseDouble(value));
                        } catch (NumberFormatException e) {
                            result.put(key, 0.0);
                        }
                    }
                    // Otros valores
                    else {
                        result.put(key, value.replaceAll("\"", ""));
                    }
                }
            }
            
            // Valores por defecto
            result.putIfAbsent("biasDetected", false);
            result.putIfAbsent("severity", 0.0);
            result.putIfAbsent("action", "allow");
            
        } catch (Exception e) {
            logger.warn("Could not parse bias JSON, using defaults");
            result.put("biasDetected", false);
            result.put("severity", 0.0);
            result.put("action", "allow");
        }
        return result;
    }
}