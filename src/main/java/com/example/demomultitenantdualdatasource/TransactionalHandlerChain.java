package com.example.demomultitenantdualdatasource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransactionalHandlerChain {

    @Bean(name = "chainedTransactionManager")
    public ChainedTransactionManager chainedTransactionManager(
            @Qualifier("tenantTransactionManager")
            PlatformTransactionManager accountingTransactionManager,
            @Qualifier("commonTransactionManager")
            PlatformTransactionManager warehouseTransactionManager) {
        return new ChainedTransactionManager(accountingTransactionManager, warehouseTransactionManager);
    }

}
