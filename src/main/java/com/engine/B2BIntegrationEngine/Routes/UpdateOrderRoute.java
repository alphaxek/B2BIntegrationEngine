package com.engine.B2BIntegrationEngine.Routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.redis.processor.idempotent.SpringRedisIdempotentRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class UpdateOrderRoute extends RouteBuilder{
    private final RedisTemplate<String, String> redisTemplate;

    UpdateOrderRoute(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void configure() throws Exception {
        errorHandler(deadLetterChannel(dlqEndpoint())
            .maximumRedeliveries(3)
            .redeliveryDelay(5000)
            .maximumRedeliveryDelay(60000)
            .useExponentialBackOff()
            .backOffMultiplier(2)
            .retryAttemptedLogLevel(LoggingLevel.WARN));

        SpringRedisIdempotentRepository redisIdRepo = new SpringRedisIdempotentRepository(redisTemplate, "duplicate-check");

        from("direct:update-order")
        .routeId("update-order")
        .idempotentConsumer(header("X-Correlation-Id"), redisIdRepo)
        .skipDuplicate(true)
        .log("Received request to update")
        .marshal().json()
        .to("kafka:{{kafka.topic.update-order}}?brokers={{kafka.bootstrap-servers}}"
            + "&securityProtocol={{kafka.security-protocol}}"
            + "&saslMechanism={{kafka.sasl-mechanism}}"
            + "&saslJaasConfig={{kafka.sasl.jaas.config}}"
            + "&sslTruststoreLocation={{kafka.ssl-truststore-location}}"
            + "&sslTruststorePassword={{kafka.ssl-truststore-password}}"
            + "&sslTruststoreType={{kafka.ssl-truststore-type}}");
    }

    
    private String dlqEndpoint() {
        return "kafka:{{kafka.topic.create-order-dlq}}?brokers={{kafka.bootstrap-servers}}"
            + "&securityProtocol={{kafka.security-protocol}}"
            + "&saslMechanism={{kafka.sasl-mechanism}}"
            + "&saslJaasConfig={{kafka.sasl.jaas.config}}"
            + "&sslTruststoreLocation={{kafka.ssl-truststore-location}}"
            + "&sslTruststorePassword={{kafka.ssl-truststore-password}}";
    }
}
