package com.smartwomen.agents;

import com.smartwomen.models.AgentRequest;
import com.smartwomen.models.AgentResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * SentimentAnalyzerAgent - Análisis de sentimientos con contexto de género
 * Especializado en entender emociones y matices específicos en comunicación femenina
 */
@Component
public class SentimentAnalyzerAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(SentimentAnalyzerAgent.class);
    
    @Autowired
    @Qualifier("openAIClient")
    private OpenAIClient openAIClient;
    
    @Autowired
    @Qualifier("textAnalyticsClient")
    private TextAnalyticsClient textAnalyticsClient;
    
    @Value("${spring.azure.cognitive-services.openai.gpt4-deployment:smartwomen-nano}")
    private String gpt4Deployment;
    
    /**
     * Analiza sentimientos con contexto específico de género y cultura LATAM
     */
    public AgentResponse analyzeSentiment(AgentRequest request) {
        try {
            String inputText = request.getContent();
            Map<String, Object> context = request.getContext();
            
            // Análisis Azure AI Language
            DocumentSentiment sentimentResult = textAnalyticsClient.analyzeSentiment(inputText);
            
            // Análisis contextual con GPT-4
            String systemPrompt = """
                Eres un experto en análisis emocional con enfoque de género y sensibilidad cultural LATAM.
                Analiza el texto considerando:
                
                1. EMOCIONES PRINCIPALES: joy, anger, sadness, fear, surprise, disgust, trust
                2. INTENSIDAD: 0.0 (muy baja) a 1.0 (muy alta)
                3. CONTEXTO DE GÉNERO: ¿Las emociones están relacionadas con experiencias específicas de mujeres?
                4. SENSIBILIDAD CULTURAL: ¿Hay matices propios de la cultura LATAM?
                5. NECESIDAD DE ESCALACIÓN: ¿La clienta necesita atención especial?
                
                DATOS DEL ANÁLISIS AZURE:
                Sentiment: """ + sentimentResult.getSentiment() + """
                Confidence Scores: """ + sentimentResult.getConfidenceScores() + """
                
                Responde SOLO en JSON:
                {
                  "primaryEmotion": "joy/anger/sadness/fear/surprise/disgust/trust",
                  "emotionIntensity": 0.0-1.0,
                  "genderContext": "descripción de cómo el género influye en la emoción",
                  "culturalContext": "matices culturales LATAM detectados",
                  "requiresEscalation": true/false,
                  "empathyLevel": "high/medium/low",
                  "recommendedResponse": "tipo de respuesta sugerida"
                }
                """;
            
            var chatRequest = new com.azure.ai.openai.models.ChatCompletionsOptions(
                List.of(new ChatRequestUserMessage(systemPrompt), 
                       new ChatRequestUserMessage("Texto: " + inputText))
            ).setModel(gpt4Deployment);
            
            var response = openAIClient.getChatCompletions(gpt4Deployment, chatRequest);
            String aiAnalysis = response.getChoices().get(0).getMessage().getContent();
            
            Map<String, Object> parsedResult = parseSentimentJson(aiAnalysis);
            Map<String, Object> metadata = new HashMap<>();
            
            // Combinar análisis Azure + AI contextual
            metadata.putAll(parsedResult);
            metadata.put("azureSentiment", sentimentResult.getSentiment().toString());
            metadata.put("azureConfidence", sentimentResult.getConfidenceScores().toString());
            
            logger.info("Sentiment analysis - Emotion: {}, Intensity: {}, Escalation: {}", 
                       parsedResult.get("primaryEmotion"),
                       parsedResult.get("emotionIntensity"),
                       parsedResult.get("requiresEscalation"));
            
            return AgentResponse.builder()
                .agentType("SentimentAnalyzer")
                .content(aiAnalysis)
                .metadata(metadata)
                .success(true)
                .processingTime(System.currentTimeMillis() - request.getTimestamp())
                .build();
                
        } catch (Exception e) {
            logger.error("Error analyzing sentiment", e);
            return AgentResponse.builder()
                .agentType("SentimentAnalyzer")
                .error("Sentiment analysis failed: " + e.getMessage())
                .success(false)
                .build();
        }
    }
    
    private Map<String, Object> parseSentimentJson(String json) {
        Map<String, Object> result = new HashMap<>();
        try {
            String cleaned = json.replaceAll("[{}]", "").trim();
            String[] pairs = cleaned.split(",");
            
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].replaceAll("\"", "").trim();
                    String value = keyValue[1].replaceAll("\"", "").trim();
                    
                    // Convertir valores numéricos
                    if (key.equals("emotionIntensity")) {
                        try {
                            result.put(key, Double.parseDouble(value));
                        } catch (NumberFormatException e) {
                            result.put(key, 0.5);
                        }
                    } else if (key.equals("requiresEscalation")) {
                        result.put(key, Boolean.parseBoolean(value));
                    } else {
                        result.put(key, value);
                    }
                }
            }
            
            // Valores por defecto seguros
            result.putIfAbsent("primaryEmotion", "neutral");
            result.putIfAbsent("emotionIntensity", 0.5);
            result.putIfAbsent("requiresEscalation", false);
            result.putIfAbsent("empathyLevel", "medium");
            
        } catch (Exception e) {
            logger.warn("Could not parse sentiment JSON, using defaults");
            result.put("primaryEmotion", "neutral");
            result.put("emotionIntensity", 0.5);
            result.put("requiresEscalation", false);
            result.put("empathyLevel", "medium");
        }
        return result;
    }
}