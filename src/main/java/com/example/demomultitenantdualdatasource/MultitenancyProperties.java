package com.example.demomultitenantdualdatasource;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "multitenancy.datasource")
public class MultitenancyProperties {
    private String commonUrl;
    private String commonUsername;
    private String commonPassword;
    private String commonDriverClassName;

    // Base properties for tenant databases
    private String tenantUrlTemplate; // e.g., "jdbc:postgresql://localhost:5432/tenant_{tenantId}"
    private String tenantUsernameTemplate;
    private String tenantPasswordTemplate;
    private String tenantDriverClassName;
    
    // Map to store dynamically registered tenants
    private Map<String, TenantDetails> tenants = new HashMap<>();
    
    @Data
    public static class TenantDetails {
        private String tenantId;
        private String url;
        private String username;
        private String password;
    }
}