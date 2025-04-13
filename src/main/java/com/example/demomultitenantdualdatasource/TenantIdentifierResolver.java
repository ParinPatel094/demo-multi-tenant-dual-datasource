package com.example.demomultitenantdualdatasource;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {
    
    private static final String DEFAULT_TENANT_ID = "default";
    private static final String TENANT_HEADER_NAME = "X-TenantID";
    
    // Thread local to hold the current tenant ID when not available from request
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    
    public String resolveCurrentTenantIdentifier() {
        // First try to get from HTTP request header
        ServletRequestAttributes requestAttributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            String tenantId = request.getHeader(TENANT_HEADER_NAME);
            
            if (tenantId != null && !tenantId.isEmpty()) {
                return tenantId;
            }
        }
        
        // If not in request, try from thread local
        String tenantId = currentTenant.get();
        return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
    }

    public void setCurrentTenant(String tenantId) {
        currentTenant.set(tenantId);
    }
    
    public void clear() {
        currentTenant.remove();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        // Return false to indicate that existing sessions should be invalidated
        // when the tenant identifier changes
        return false;
    }
}