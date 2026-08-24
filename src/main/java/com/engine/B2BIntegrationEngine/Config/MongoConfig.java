package com.engine.B2BIntegrationEngine.Config;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    @Bean(name = "myMongoClient")
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://localhost:27017");
    }
}

