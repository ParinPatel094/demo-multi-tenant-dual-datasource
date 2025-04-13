package com.example.demomultitenantdualdatasource;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
@RequiredArgsConstructor
public class JdbcTemplateService {

    @Qualifier("commonDataSource")
    private final DataSource commonDataSource;
    
    private final MultiTenantConnectionProvider tenantConnectionProvider;
    private final TenantIdentifierResolver tenantIdentifierResolver;

    /**
     * Get a NamedParameterJdbcTemplate for the common database
     */
    public NamedParameterJdbcTemplate getCommonJdbcTemplate() {
        return new NamedParameterJdbcTemplate(commonDataSource);
    }

    /**
     * Get a NamedParameterJdbcTemplate for the current tenant database
     */
    public NamedParameterJdbcTemplate getCurrentTenantJdbcTemplate() {
        String currentTenant = tenantIdentifierResolver.resolveCurrentTenantIdentifier();
        if (currentTenant == null || currentTenant.isEmpty()) {
            throw new IllegalStateException("No current tenant has been set");
        }
        
        // Get connection for the current tenant
        DataSource tenantDataSource = new TenantAwareDataSource(
                tenantConnectionProvider, 
                tenantIdentifierResolver
        );
        
        return new NamedParameterJdbcTemplate(tenantDataSource);
    }
    
    /**
     * Get a NamedParameterJdbcTemplate for a specific tenant database
     */
    public NamedParameterJdbcTemplate getTenantJdbcTemplate(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            throw new IllegalArgumentException("Tenant ID must not be null or empty");
        }
        
        // Create a temporary tenant resolver that always returns the specified tenant
        TenantIdentifierResolver specificTenantResolver = new TenantIdentifierResolver() {
            @Override
            public String resolveCurrentTenantIdentifier() {
                return tenantId;
            }
            
            @Override
            public boolean validateExistingCurrentSessions() {
                return true;
            }
            
            @Override
            public void setCurrentTenant(String tenant) {
                // No-op as this is a temporary resolver
            }
        };
        
        // Get connection for the specified tenant
        DataSource tenantDataSource = new TenantAwareDataSource(
                tenantConnectionProvider, 
                specificTenantResolver
        );
        
        return new NamedParameterJdbcTemplate(tenantDataSource);
    }
}