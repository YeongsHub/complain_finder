package com.findcomplain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "reddit")
public class RedditApiConfig {
    private String clientId;
    private String clientSecret;
    private String userAgent;
    private String username;
    private String password;
    private boolean mockMode = true;
}
