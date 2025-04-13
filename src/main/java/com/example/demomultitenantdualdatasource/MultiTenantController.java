package com.example.demomultitenantdualdatasource;

import com.example.demomultitenantdualdatasource.common.CommonSettings;
import com.example.demomultitenantdualdatasource.common.CommonSettingsRepository;
import com.example.demomultitenantdualdatasource.tenant.TenantData;
import com.example.demomultitenantdualdatasource.tenant.TenantDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MultiTenantController {
    
    private final CommonSettingsRepository commonSettingsRepository;
    private final TenantDataRepository tenantDataRepository;
    private final TenantManagementService tenantManagementService;
    private final JdbcTemplateService jdbcTemplateService;

    @PostMapping("/tenants")
    public ResponseEntity<String> registerTenant(@RequestParam String tenantId) {
        tenantManagementService.registerNewTenant(tenantId);
        return ResponseEntity.ok("Tenant registered successfully: " + tenantId);
    }

    @Transactional(transactionManager = "commonTransactionManager")
    @PostMapping("/common/settings")
    public ResponseEntity<CommonSettings> addCommonSetting(@RequestBody CommonSettings settings) {
        return ResponseEntity.ok(commonSettingsRepository.save(settings));
    }
    
    @GetMapping("/common/settings")
    public ResponseEntity<?> getAllCommonSettings() {
        return ResponseEntity.ok(commonSettingsRepository.findAll());
    }


    @PostMapping("/tenant/data")
    @Transactional(transactionManager = "tenantTransactionManager")
    public ResponseEntity<TenantData> addTenantData(@RequestBody TenantData data) {
        return ResponseEntity.ok(tenantDataRepository.save(data));
    }
    
    @GetMapping("/tenant/data")
    public ResponseEntity<?> getTenantData() {
        return ResponseEntity.ok(tenantDataRepository.findAll());
    }

    /**
     * Get data from both tenant and common databases in a single API call
     */
    @GetMapping("/combined-data")
    public ResponseEntity<?> getCombinedData() {
        // Get data from tenant database
        List<TenantData> tenantData = tenantDataRepository.findAll();

        // Get data from common database
        List<CommonSettings> commonSettings = commonSettingsRepository.findAll();

        // Combine the results
        Map<String, Object> result = new HashMap<>();
        result.put("tenantData", tenantData);
        result.put("commonSettings", commonSettings);

        return ResponseEntity.ok(result);
    }

    /**
     * Save data to both tenant and common databases in a single API call
     */
    @PostMapping("/combined-data")
    @Transactional(transactionManager = "chainedTransactionManager")
    public ResponseEntity<?> addCombinedData(
            @RequestBody TenantData tenantData,
            @RequestBody CommonSettings commonSettings) {

        // Save to tenant database
        TenantData savedTenantData = tenantDataRepository.save(tenantData);

        // Save to common database
        CommonSettings savedCommonSettings = commonSettingsRepository.save(commonSettings);

        // Return combined result
        Map<String, Object> result = new HashMap<>();
        result.put("tenantData", savedTenantData);
        result.put("commonSettings", savedCommonSettings);

        return ResponseEntity.ok(result);
    }

    /**
     * Alternative approach using a combined DTO
     */
    @PostMapping("/combined-data-dto")
    public ResponseEntity<?> addCombinedDataUsingDto(@RequestBody CombinedDataDto dto) {
        // Save to tenant database
        TenantData savedTenantData = tenantDataRepository.save(dto.getTenantData());

        // Save to common database
        CommonSettings savedCommonSettings = commonSettingsRepository.save(dto.getCommonSettings());

        // Return combined result
        Map<String, Object> result = new HashMap<>();
        result.put("tenantData", savedTenantData);
        result.put("commonSettings", savedCommonSettings);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/tenant/dynamic-query")
    public ResponseEntity<?> executeDynamicTenantQuery(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String value) {

        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM tenant_data WHERE 1=1");
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (name != null && !name.isEmpty()) {
            queryBuilder.append(" AND name LIKE :name");
            params.addValue("name", "%" + name + "%");
        }

        if (value != null && !value.isEmpty()) {
            queryBuilder.append(" AND value = :value");
            params.addValue("value", value);
        }

        // Get JDBC template for current tenant
        NamedParameterJdbcTemplate jdbcTemplate = jdbcTemplateService.getCurrentTenantJdbcTemplate();

        // Execute the query
        List<Map<String, Object>> results = jdbcTemplate.queryForList(queryBuilder.toString(), params);

        return ResponseEntity.ok(results);
    }



}