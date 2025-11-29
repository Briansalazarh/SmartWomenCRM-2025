package com.smartwomen.agents;

import com.smartwomen.models.AgentRequest;
import com.smartwomen.models.AgentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import com.azure.ai.openai.OpenAIClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component
public class PlannerAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(PlannerAgent.class);
    
    @Autowired
    @Qualifier("openAIClient")
    private OpenAIClient openAIClient;
    
    @Value("${azure.openai.gpt4-deployment}")
    private String gpt4Deployment;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Planifica la ejecución de agentes basada en el contexto
     */
    public AgentResponse createExecutionPlan(AgentRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            String inputText = request.getContent();
            Map<String, Object> context = request.getContext();
            String contextSummary = extractContextSummary(context);
            
            logger.info("🌍 PLAN INPUT: [{}]", inputText);
            logger.info("📋 CONTEXT: {}", contextSummary);
            
            // Prompt especializado para planificación
            String systemPrompt = buildPlannerPrompt(contextSummary);
            
            var chatRequest = new com.azure.ai.openai.models.ChatCompletionsOptions(
                List.of(
                    new com.azure.ai.openai.models.ChatRequestSystemMessage(systemPrompt),
                    new com.azure.ai.openai.models.ChatRequestUserMessage("Mensaje: " + inputText)
                )
            ).setModel(gpt4Deployment);
            
            var response = openAIClient.getChatCompletions(gpt4Deployment, chatRequest);
            String aiPlan = response.getChoices().get(0).getMessage().getContent();
            logger.info("🤖 GPT-4 PLAN: {}", aiPlan);
            
            // Parsear y estructurar resultado
            Map<String, Object> planResult = parsePlanJson(aiPlan);
            
            // Metadata limpia
            Map<String, Object> metadata = buildMetadata(planResult);
            
            return AgentResponse.builder()
                .agentType("Planner")
                .content(objectMapper.writeValueAsString(planResult))  // ✅ JSON parseable
                .metadata(metadata)
                .success(true)
                .processingTime(System.currentTimeMillis() - startTime)
                .confidence(String.valueOf(planResult.get("confidence")))  // ✅ Confidence extraído
                .build();
                
        } catch (Exception e) {
            logger.error("❌ PLAN CREATION FAILED", e);
            return buildErrorResponse(e.getMessage(), request.getTimestamp());
        }
    }
    
    private String buildPlannerPrompt(String contextSummary) {
        return """
            Eres el PlannerAgent - el cerebro orquestador de SmartWomen CRM.
            Tu función es crear un plan inteligente de ejecución de agentes.
            
            AGENTES DISPONIBLES:
            - LanguageDetector: Detecta idioma y dialecto LATAM
            - SentimentAnalyzer: Analiza sentimientos con contexto de género
            - BiasGuard: Detecta y bloquea sesgos
            - Personalization: Personaliza contenido usando RAG
            - ResponseGenerator: Genera respuestas empáticas
            - Handoff: Escalación a humanos si es necesario
            
            CONTEXTO DEL USUARIO:
            """ + contextSummary + """
            
            CONSIDERACIONES:
            1. LanguageDetector primero (si mensaje nuevo)
            2. Paralelizar agentes independientes
            3. Cachear resultados similares
            4. Triggers condicionales (sentiment > 0.9 → Handoff)
            
            Responde SOLO en JSON:
            {
              "executionOrder": ["Agent1", "Agent2"],
              "parallelAgents": [["Agent1", "Agent2"]],
              "reasoning": "explicación",
              "estimatedTime": 500,
              "complexity": "low/medium/high",
              "agentsToCache": ["Agent1"],
              "conditionalTriggers": {"condition": "agent"},
              "confidence": 0.85
            }
            """;
    }
    
    private Map<String, Object> parsePlanJson(String json) {
        try {
            String cleaned = json.replaceAll("```json", "").replaceAll("```", "").trim();
            Map<String, Object> plan = objectMapper.readValue(cleaned, Map.class);
            
            // Valores por defecto si faltan
            plan.putIfAbsent("confidence", 0.8);
            plan.putIfAbsent("estimatedTime", 1000);
            plan.putIfAbsent("complexity", "medium");
            
            return plan;
        } catch (Exception e) {
            logger.warn("❌ Parse error: {}", e.getMessage());
            return createFallbackPlan();
        }
    }
    
    private Map<String, Object> createFallbackPlan() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("executionOrder", List.of("LanguageDetector", "SentimentAnalyzer", "ResponseGenerator"));
        fallback.put("parallelAgents", List.of());
        fallback.put("reasoning", "Fallback plan due to parse error");
        fallback.put("estimatedTime", 1500);
        fallback.put("complexity", "medium");
        fallback.put("agentsToCache", List.of());
        fallback.put("conditionalTriggers", Map.of());
        fallback.put("confidence", 0.5);
        return fallback;
    }
    
    private Map<String, Object> buildMetadata(Map<String, Object> planResult) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("executionOrder", planResult.get("executionOrder"));
        metadata.put("parallelAgents", planResult.get("parallelAgents"));
        metadata.put("estimatedTime", planResult.get("estimatedTime"));
        metadata.put("complexity", planResult.get("complexity"));
        metadata.put("agentsToCache", planResult.get("agentsToCache"));
        metadata.put("conditionalTriggers", planResult.get("conditionalTriggers"));
        return metadata;
    }
    
    private String extractContextSummary(Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return "No context - new conversation";
        }
        
        StringBuilder summary = new StringBuilder();
        if (context.containsKey("userType")) {
            summary.append("User: ").append(context.get("userType")).append(". ");
        }
        if (context.containsKey("conversationLength")) {
            summary.append("Length: ").append(context.get("conversationLength")).append(". ");
        }
        if (context.containsKey("requestType")) {
            summary.append("Type: ").append(context.get("requestType")).append(". ");
        }
        return summary.toString();
    }
    
    private AgentResponse buildErrorResponse(String error, long timestamp) {
        return AgentResponse.builder()
                .agentType("Planner")
                .content("Plan creation failed")
                .error(error)
                .success(false)
                .processingTime(System.currentTimeMillis() - timestamp)
                .confidence("0.0")
                .build();
    }
}