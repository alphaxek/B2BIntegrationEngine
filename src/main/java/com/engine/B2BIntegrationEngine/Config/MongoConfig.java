package com.engine.B2BIntegrationEngine.Config;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class MongoConfig {

    @Bean(name = "myMongoClient")
    public MongoClient mongoClient(@Value("${spring.data.mongodb.uri}") String mongoUri) {
        return MongoClients.create(mongoUri);
    }
}

