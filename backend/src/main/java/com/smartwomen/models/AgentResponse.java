package com.smartwomen.models;

import lombok.Data;
import lombok.Builder;
import java.util.Map;
import java.util.List;

/**
 * Response model para agentes especializados
 */
@Data
@Builder
public class AgentResponse {
    private String agentType;
    private String content;
    private Map<String, Object> metadata;
    private String error;
    private boolean success;
    private long processingTime;
    private String confidence;
    private List<String> suggestions;
    private Map<String, Object> analytics;
    
    public static AgentResponseBuilder builder() {
        return new AgentResponseBuilder();
    }
    
    public static class AgentResponseBuilder {
        private boolean success = true;
        private String agentType = "Unknown";
        private long processingTime = 0;
        
        public AgentResponseBuilder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public AgentResponseBuilder agentType(String agentType) {
            this.agentType = agentType;
            return this;
        }
    }
}