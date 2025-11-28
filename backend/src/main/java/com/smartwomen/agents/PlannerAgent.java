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
import java.util.ArrayList;
import java.util.Arrays;

/**
 * PlannerAgent - Orquestador inteligente de agentes
 * Decide qué agentes ejecutar y en qué orden basado en el contexto y objetivos
 */
@Component
public class PlannerAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(PlannerAgent.class);
    
    @Autowired
    @Qualifier("openAIClient")
    private OpenAIClient openAIClient;
    
    @Value("${spring.azure.cognitive-services.openai.gpt4-deployment:smartwomen-nano}")
    private String gpt4Deployment;
    
    // Agentes disponibles en el sistema
    private static final List<String> AVAILABLE_AGENTS = Arrays.asList(
        "LanguageDetector", "SentimentAnalyzer", "BiasGuard", 
        "Personalization", "ResponseGenerator", "Handoff"
    );
    
    /**
     * Planifica la ejecución de agentes basada en el contexto
     */
    public AgentResponse createExecutionPlan(AgentRequest request) {
        try {
            String inputText = request.getContent();
            Map<String, Object> context = request.getContext();
            
            // Extraer contexto para la planificación
            String contextSummary = extractContextSummary(context);
            
            String systemPrompt = """
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
                
                CONSIDERACIONES DE PLANIFICACIÓN:
                1. Siempre ejecutar LanguageDetector PRIMERO (si es mensaje nuevo)
                2. Ejecutar SentimentAnalyzer en mensajes importantes
                3. Ejecutar BiasGuard si el contenido es sensible o generado por IA
                4. Ejecutar Personalization para contenido personalizado
                5. Ejecutar ResponseGenerator para generar respuestas finales
                6. Ejecutar Handoff solo si sentiment intensity > 0.9 O bias severity > 0.8
                
                CRITERIOS DE EFICIENCIA:
                - Minimizar llamadas innecesarias
                - Paralelizar agentes independientes cuando sea posible
                - Cachear resultados similares
                
                Responde SOLO en JSON:
                {
                  "executionOrder": ["Agent1", "Agent2", ...],
                  "parallelAgents": [["Agent1", "Agent2"], ["Agent3"]],
                  "reasoning": "explicación del plan",
                  "estimatedTime": "tiempo estimado en ms",
                  "complexity": "low/medium/high",
                  "agentsToCache": ["Agent1", "Agent3"],
                  "conditionalTriggers": {
                    "ifSentimentHigh": "Handoff",
                    "ifBiasDetected": "BiasGuard"
                  }
                }
                """;
            
            var chatRequest = new com.azure.ai.openai.models.ChatCompletionsOptions(
                List.of(new ChatRequestUserMessage(systemPrompt), 
                       new ChatRequestUserMessage("Mensaje del usuario: " + inputText))
            ).setModel(gpt4Deployment);
            
            var response = openAIClient.getChatCompletions(gpt4Deployment, chatRequest);
            String planJson = response.getChoices().get(0).getMessage().getContent();
            
            Map<String, Object> executionPlan = parsePlanJson(planJson);
            
            logger.info("Execution plan created - Order: {}, Complexity: {}", 
                       executionPlan.get("executionOrder"),
                       executionPlan.get("complexity"));
            
            return AgentResponse.builder()
                .agentType("Planner")
                .content(planJson)
                .metadata(executionPlan)
                .success(true)
                .processingTime(System.currentTimeMillis() - request.getTimestamp())
                .build();
                
        } catch (Exception e) {
            logger.error("Error creating execution plan", e);
            
            // Plan de emergencia
            Map<String, Object> fallbackPlan = createFallbackPlan();
            return AgentResponse.builder()
                .agentType("Planner")
                .content("Fallback execution plan")
                .metadata(fallbackPlan)
                .success(false)
                .build();
        }
    }
    
    /**
     * Plan de ejecución por defecto en caso de error
     */
    private Map<String, Object> createFallbackPlan() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("executionOrder", Arrays.asList("LanguageDetector", "SentimentAnalyzer", "ResponseGenerator"));
        fallback.put("parallelAgents", Arrays.asList(Arrays.asList("LanguageDetector", "SentimentAnalyzer")));
        fallback.put("reasoning", "Default safe execution path");
        fallback.put("estimatedTime", "1500");
        fallback.put("complexity", "low");
        return fallback;
    }
    
    /**
     * Extrae un resumen del contexto para la planificación
     */
    private String extractContextSummary(Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return "No context available - new conversation";
        }
        
        StringBuilder summary = new StringBuilder();
        
        if (context.containsKey("userType")) {
            summary.append("Usuario: ").append(context.get("userType")).append(". ");
        }
        if (context.containsKey("previousAgentResults")) {
            summary.append("Resultados previos disponibles. ");
        }
        if (context.containsKey("conversationLength")) {
            summary.append("Longitud conversación: ").append(context.get("conversationLength")).append(". ");
        }
        if (context.containsKey("requestType")) {
            summary.append("Tipo solicitud: ").append(context.get("requestType")).append(". ");
        }
        
        return summary.length() > 0 ? summary.toString() : "Context available";
    }
    
    private Map<String, Object> parsePlanJson(String json) {
        Map<String, Object> result = new HashMap<>();
        try {
            String cleaned = json.replaceAll("[{}]", "").trim();
            String[] pairs = cleaned.split(",");
            
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length >= 2) {
                    String key = keyValue[0].replaceAll("\"", "").trim();
                    String value = keyValue[1].trim();
                    
                    if (key.equals("executionOrder") || key.equals("agentsToCache")) {
                        // Arrays - extraer contenido de brackets
                        String arrayContent = value.replaceAll("\\[|\\]", "").trim();
                        List<String> agents = new ArrayList<>();
                        for (String agent : arrayContent.split("\",\"")) {
                            agents.add(agent.replaceAll("\"", "").trim());
                        }
                        result.put(key, agents);
                    } else if (key.equals("parallelAgents")) {
                        // Nested arrays - simplificado
                        result.put(key, value);
                    } else if (key.equals("conditionalTriggers")) {
                        result.put(key, value);
                    } else {
                        result.put(key, value.replaceAll("\"", ""));
                    }
                }
            }
            
            // Validar que el plan contenga agentes válidos
            if (!result.containsKey("executionOrder")) {
                result.put("executionOrder", Arrays.asList("LanguageDetector", "SentimentAnalyzer"));
            }
            
        } catch (Exception e) {
            logger.warn("Could not parse plan JSON, using fallback");
            result = createFallbackPlan();
        }
        return result;
    }
}