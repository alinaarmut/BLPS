package org.example.config;


import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;


import jakarta.annotation.Resource;
import jakarta.transaction.UserTransaction;

@Configuration
@RequiredArgsConstructor
public class JpaConfiguration {

        @Resource(lookup = "java:jboss/UserTransaction")
        private UserTransaction userTransaction;

        @Bean
        public UserTransaction userTransaction() {
            return userTransaction;
        }

        @Bean
        public PlatformTransactionManager transactionManager() {
            JtaTransactionManager transactionManager = new JtaTransactionManager();
            transactionManager.setUserTransaction(userTransaction);
            return transactionManager;
        }

}

