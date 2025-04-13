package com.example.demomultitenantdualdatasource;

import com.example.demomultitenantdualdatasource.common.CommonSettings;
import com.example.demomultitenantdualdatasource.tenant.TenantData;

/**
 * DTO for combined data operations
 */
class CombinedDataDto {
    private TenantData tenantData;
    private CommonSettings commonSettings;
    
    // Getters and setters
    public TenantData getTenantData() {
        return tenantData;
    }
    
    public void setTenantData(TenantData tenantData) {
        this.tenantData = tenantData;
    }
    
    public CommonSettings getCommonSettings() {
        return commonSettings;
    }
    
    public void setCommonSettings(CommonSettings commonSettings) {
        this.commonSettings = commonSettings;
    }
}
