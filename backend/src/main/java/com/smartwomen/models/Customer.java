package com.smartwomen.models;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.Map;

@Container(containerName = "customers")
public class Customer {
    
    @Id
    private String id;
    
    @PartitionKey
    private String customerId;
    
    private String name;
    private String email;
    private String country;
    private String lastMessage;
    private Map<String, Object> lastAgentResults;
    private LocalDateTime createdAt;
    private LocalDateTime lastInteraction;
    private String industry;
    private String businessSize;

    public Customer() {
        this.createdAt = LocalDateTime.now();
        this.lastInteraction = LocalDateTime.now();
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    
    public Map<String, Object> getLastAgentResults() { return lastAgentResults; }
    public void setLastAgentResults(Map<String, Object> lastAgentResults) { this.lastAgentResults = lastAgentResults; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastInteraction() { return lastInteraction; }
    public void setLastInteraction(LocalDateTime lastInteraction) { this.lastInteraction = lastInteraction; }
    
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    
    public String getBusinessSize() { return businessSize; }
    public void setBusinessSize(String businessSize) { this.businessSize = businessSize; }
}   