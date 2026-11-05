package com.evidence_engine.backend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public GroqClient groqClient(@Value("${groq.api-key:${GROQ_API_KEY:}}") String apiKey) {
        return new GroqClient(apiKey);
    }

    @Bean
    public QueryPlanner queryPlanner(GroqClient groq,
                                     @Value("${groq.query-model:" + QueryPlanner.DEFAULT_MODEL + "}") String model) {
        return new QueryPlanner(groq, model);
    }

    @Bean
    public SourceRanker sourceRanker(GroqClient groq,
                                     @Value("${groq.ranking-model:" + SourceRanker.DEFAULT_MODEL + "}") String model) {
        return new SourceRanker(groq, model);
    }
}
