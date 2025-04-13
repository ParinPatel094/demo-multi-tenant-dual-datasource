package com.example.demomultitenantdualdatasource;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl<String> {

    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();
    private final MultitenancyProperties multitenancyProperties;

    public MultiTenantConnectionProvider(MultitenancyProperties multitenancyProperties) {
        this.multitenancyProperties = multitenancyProperties;
    }

    @Override
    protected DataSource selectAnyDataSource() {
        // Return a default tenant database or create one if it doesn't exist yet
        return dataSources.computeIfAbsent("default", this::createDataSource);
    }

    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        return dataSources.computeIfAbsent(tenantIdentifier, this::createDataSource);
    }

    private DataSource createDataSource(String tenantId) {
        log.info("Creating new datasource for tenant: {}", tenantId);

        // Check if we have specific configuration for this tenant
        MultitenancyProperties.TenantDetails tenantDetails =
                multitenancyProperties.getTenants().get(tenantId);

        if (tenantDetails != null) {
            return DataSourceBuilder.create()
                    .url(tenantDetails.getUrl())
                    .username(tenantDetails.getUsername())
                    .password(tenantDetails.getPassword())
                    .driverClassName(multitenancyProperties.getTenantDriverClassName())
                    .type(HikariDataSource.class)
                    .build();
        } else {
            // Use template to create database URL
            String url = multitenancyProperties.getTenantUrlTemplate().replace("{tenantId}", tenantId);
            String username = multitenancyProperties.getTenantUsernameTemplate().replace("{tenantId}", tenantId);
            String password = multitenancyProperties.getTenantPasswordTemplate().replace("{tenantId}", tenantId);

            return DataSourceBuilder.create()
                    .url(url)
                    .username(username)
                    .password(password)
                    .driverClassName(multitenancyProperties.getTenantDriverClassName())
                    .type(HikariDataSource.class)
                    .build();
        }
    }

    // Method to add a new tenant database
    public void addTenant(String tenantId, String url, String username, String password) {
        // Add to properties for persistence
        MultitenancyProperties.TenantDetails details = new MultitenancyProperties.TenantDetails();
        details.setTenantId(tenantId);
        details.setUrl(url);
        details.setUsername(username);
        details.setPassword(password);
        multitenancyProperties.getTenants().put(tenantId, details);

        // Create and add the datasource
        DataSource dataSource = DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(multitenancyProperties.getTenantDriverClassName())
                .type(HikariDataSource.class)
                .build();

        dataSources.put(tenantId, dataSource);
        log.info("Added new tenant: {}", tenantId);
    }
}