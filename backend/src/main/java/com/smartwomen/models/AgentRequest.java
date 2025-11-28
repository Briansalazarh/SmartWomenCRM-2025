package com.smartwomen.models;

import lombok.Data;
import lombok.Builder;
import java.util.Map;
import java.util.List;

/**
 * Request model para agentes especializados
 */
@Data
@Builder
public class AgentRequest {
    private String agentType;
    private String content;
    private Map<String, Object> context;
    private List<String> parameters;
    private long timestamp;
    private String userId;
    private String sessionId;
    private String requestId;
    
    public static AgentRequestBuilder builder() {
        return new AgentRequestBuilder();
    }
    
    public static class AgentRequestBuilder {
        private long timestamp = System.currentTimeMillis();
        private String requestId = "req_" + System.currentTimeMillis();
        
        public AgentRequestBuilder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public AgentRequestBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }
    }
}