package com.example.demomultitenantdualdatasource;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.demomultitenantdualdatasource.common",
        entityManagerFactoryRef = "commonEntityManagerFactory",  // Using the standard name
        transactionManagerRef = "tenantTransactionManager"       // Using the standard name
)
public class DatasourceConfigCommon {

    private final MultitenancyProperties multitenancyProperties;

    public DatasourceConfigCommon(MultitenancyProperties multitenancyProperties) {
        this.multitenancyProperties = multitenancyProperties;
    }

    // Configure common database
    @Bean(name = "commonDataSource")
    @Primary
    public DataSource commonDataSource() {
        return DataSourceBuilder.create()
                .url(multitenancyProperties.getCommonUrl())
                .username(multitenancyProperties.getCommonUsername())
                .password(multitenancyProperties.getCommonPassword())
                .driverClassName(multitenancyProperties.getCommonDriverClassName())
                .type(HikariDataSource.class)
                .build();
    }
    
    @Bean(name = "commonEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean commonEntityManagerFactory(
            @Qualifier("commonDataSource") DataSource dataSource) {
        
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.demomultitenantdualdatasource.common");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.show_sql", "true");
        em.setJpaProperties(properties);
        
        return em;
    }

    @Bean
    @Primary
    public PlatformTransactionManager commonTransactionManager(
            @Qualifier("commonEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}