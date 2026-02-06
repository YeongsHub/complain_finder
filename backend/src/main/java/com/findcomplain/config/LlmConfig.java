package com.findcomplain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LlmConfig {
    private String provider = "openai";
    private String apiKey;
    private String model = "gpt-4o-mini";
    private boolean mockMode = true;
}
