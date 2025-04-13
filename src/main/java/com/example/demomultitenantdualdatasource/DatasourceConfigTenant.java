package com.example.demomultitenantdualdatasource;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.demomultitenantdualdatasource.tenant",
        entityManagerFactoryRef = "tenantEntityManagerFactory",  // Using the standard name
        transactionManagerRef = "tenantTransactionManager"       // Using the standard name
)
public class DatasourceConfigTenant {
    
    private final MultitenancyProperties multitenancyProperties;
    
    public DatasourceConfigTenant(MultitenancyProperties multitenancyProperties) {
        this.multitenancyProperties = multitenancyProperties;
    }

    // Configure tenant-specific database config
    @Bean(name = "tenantEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory(
            TenantIdentifierResolver tenantIdentifierResolver) {
        
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setPackagesToScan("com.example.demomultitenantdualdatasource.tenant");
        em.setPersistenceUnitName("tenant-persistence-unit");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", "true");
        
        // Multitenancy configuration
        properties.put("hibernate.multiTenancy", "DATABASE");
        properties.put("hibernate.tenant_identifier_resolver", tenantIdentifierResolver);
        properties.put("hibernate.multi_tenant_connection_provider", tenantConnectionProvider());
        em.setJpaPropertyMap(properties);
        return em;
    }
    
    @Bean
    public MultiTenantConnectionProvider tenantConnectionProvider() {
        return new MultiTenantConnectionProvider(multitenancyProperties);
    }
    
    @Bean(name = "tenantTransactionManager")
    public PlatformTransactionManager tenantTransactionManager(
            @Qualifier("tenantEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}