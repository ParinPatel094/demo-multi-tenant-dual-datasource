package com.example.demomultitenantdualdatasource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantManagementService {
    
    private final MultitenancyProperties multitenancyProperties;
    private final MultiTenantConnectionProvider connectionProvider;
    private final TenantIdentifierResolver tenantResolver;
    
    /**
     * Creates a new tenant with its own database.
     */
    public void registerNewTenant(String tenantId) {
        log.info("Registering new tenant: {}", tenantId);
        
        // Create the database for the tenant
        createTenantDatabase(tenantId);
        
        // Generate the details for the tenant
        String url = multitenancyProperties.getTenantUrlTemplate().replace("{tenantId}", tenantId);
        String username = multitenancyProperties.getTenantUsernameTemplate().replace("{tenantId}", tenantId);
        String password = multitenancyProperties.getTenantPasswordTemplate().replace("{tenantId}", tenantId);
        
        // Register the tenant with the connection provider
        connectionProvider.addTenant(tenantId, url, username, password);
        
        log.info("Tenant {} registered successfully", tenantId);
    }
    
    /**
     * Creates a new database for the tenant
     */
    private void createTenantDatabase(String tenantId) {
        String dbName = "tenant_" + tenantId;
        
        // Connect to the PostgreSQL server using the common connection
        try (Connection connection = DriverManager.getConnection(
                multitenancyProperties.getCommonUrl(),
                multitenancyProperties.getCommonUsername(),
                multitenancyProperties.getCommonPassword())) {
            
            // Create database if it doesn't exist
            try (Statement statement = connection.createStatement()) {
                // Make it safe by using CREATE DATABASE IF NOT EXISTS
                statement.execute("CREATE DATABASE " + dbName);
                log.info("Created database {} for tenant {}", dbName, tenantId);
            }
        } catch (SQLException e) {
            log.error("Failed to create database for tenant: " + tenantId, e);
            throw new RuntimeException("Failed to create tenant database", e);
        }
    }
    
    /**
     * Set the current tenant for operations not initiated via HTTP
     */
    public void setCurrentTenant(String tenantId) {
        tenantResolver.setCurrentTenant(tenantId);
    }
}