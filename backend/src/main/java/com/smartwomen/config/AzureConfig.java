package com.smartwomen.config;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.contentsafety.ContentSafetyClient;
import com.azure.ai.contentsafety.ContentSafetyClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Azure SDKs para SmartWomen CRM
 */
@Configuration
public class AzureConfig {
    
    @Value("${spring.azure.cognitive-services.openai.endpoint}")
    private String openAiEndpoint;
    
    @Value("${spring.azure.cognitive-services.openai.api-key}")
    private String openAiKey;
    
    @Value("${spring.azure.cognitive-services.content-safety.endpoint}")
    private String contentSafetyEndpoint;
    
    @Value("${spring.azure.cognitive-services.content-safety.api-key}")
    private String contentSafetyKey;
    
    @Value("${spring.azure.cognitive-services.text-analytics.endpoint}")
    private String textAnalyticsEndpoint;
    
    @Value("${spring.azure.cognitive-services.text-analytics.api-key}")
    private String textAnalyticsKey;
    
    /**
     * Cliente Azure OpenAI
     */
    @Bean
    @Qualifier("openAIClient")
    public OpenAIClient openAIClient() {
        return new OpenAIClientBuilder()
            .endpoint(openAiEndpoint)
            .credential(new AzureKeyCredential(openAiKey))
            .buildClient();
    }
    
    /**
     * Cliente Text Analytics (Para Sentiment Analysis)
     */
    @Bean
    @Qualifier("textAnalyticsClient")
    public TextAnalyticsClient textAnalyticsClient() {
        // Nota: En producción usar la región correcta
        return new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential(textAnalyticsKey))
            .endpoint(textAnalyticsEndpoint) // Usar su propio endpoint
            .buildClient();
    }
    
    /**
     * Cliente Content Safety
     */
    @Bean
    @Qualifier("contentSafetyClient")
    public ContentSafetyClient contentSafetyClient() {
        return new ContentSafetyClientBuilder()
            .endpoint(contentSafetyEndpoint)
            .credential(new AzureKeyCredential(contentSafetyKey))
            .buildClient();
    }
    
    // Removido HttpClient personalizado - Azure SDK usará su cliente por defecto
}