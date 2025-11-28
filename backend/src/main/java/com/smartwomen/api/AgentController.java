package com.smartwomen.api;

import com.smartwomen.models.AgentRequest;
import com.smartwomen.models.AgentResponse;
import com.smartwomen.agents.LanguageDetectorAgent;
import com.smartwomen.agents.SentimentAnalyzerAgent;
import com.smartwomen.agents.BiasGuardAgent;
import com.smartwomen.agents.PlannerAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;

/**
 * REST API para testing de agentes individuales
 */
@RestController
@RequestMapping("/agents")
public class AgentController {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);
    
    @Autowired
    private LanguageDetectorAgent languageDetectorAgent;
    
    @Autowired
    private SentimentAnalyzerAgent sentimentAnalyzerAgent;
    
    @Autowired
    private BiasGuardAgent biasGuardAgent;
    
    @Autowired
    private PlannerAgent plannerAgent;
    
    /**
     * POST /api/v1/agents/language-detect
     */
    @PostMapping("/language-detect")
    public AgentResponse detectLanguage(@RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.get("content");
            
            //log de prueba para el backend
            System.out.println("🟢 [LanguageDetector] request recibido: " + content); // LOG
            
            AgentRequest agentRequest = AgentRequest.builder()
                .agentType("LanguageDetector")
                .content(content)
                .context(request)
                .build();
            
            return languageDetectorAgent.detectLanguage(agentRequest);
            
        } catch (Exception e) {
            logger.error("Error in language detection endpoint", e);
            return AgentResponse.builder()
                .agentType("LanguageDetector")
                .error("Internal server error: " + e.getMessage())
                .success(false)
                .build();
        }
    }
    
    /**
     * POST /api/v1/agents/sentiment-analyze
     */
    @PostMapping("/sentiment-analyze")
    public AgentResponse analyzeSentiment(@RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.get("content");
            
            AgentRequest agentRequest = AgentRequest.builder()
                .agentType("SentimentAnalyzer")
                .content(content)
                .context(request)
                .build();
            
            return sentimentAnalyzerAgent.analyzeSentiment(agentRequest);
            
        } catch (Exception e) {
            logger.error("Error in sentiment analysis endpoint", e);
            return AgentResponse.builder()
                .agentType("SentimentAnalyzer")
                .error("Internal server error: " + e.getMessage())
                .success(false)
                .build();
        }
    }
    
    /**
     * POST /api/v1/agents/bias-detect
     */
    @PostMapping("/bias-detect")
    public AgentResponse detectBias(@RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.get("content");
            
            AgentRequest agentRequest = AgentRequest.builder()
                .agentType("BiasGuard")
                .content(content)
                .context(request)
                .build();
            
            return biasGuardAgent.detectBias(agentRequest);
            
        } catch (Exception e) {
            logger.error("Error in bias detection endpoint", e);
            return AgentResponse.builder()
                .agentType("BiasGuard")
                .error("Internal server error: " + e.getMessage())
                .success(false)
                .build();
        }
    }
    
    /**
     * POST /api/v1/agents/create-plan
     */
    @PostMapping("/create-plan")
    public AgentResponse createExecutionPlan(@RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.get("content");
            
            AgentRequest agentRequest = AgentRequest.builder()
                .agentType("Planner")
                .content(content)
                .context(request)
                .build();
            
            return plannerAgent.createExecutionPlan(agentRequest);
            
        } catch (Exception e) {
            logger.error("Error in planning endpoint", e);
            return AgentResponse.builder()
                .agentType("Planner")
                .error("Internal server error: " + e.getMessage())
                .success(false)
                .build();
        }
    }
    
    /**
     * GET /api/v1/agents/health
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("timestamp", System.currentTimeMillis());
        response.put("agents", "4 agents initialized");
        response.put("project", "SmartWomen CRM - Day 1 Core Backend");
        return response;
    }
}