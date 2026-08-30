package com.engine.B2BIntegrationEngine.Routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.redis.processor.idempotent.SpringRedisIdempotentRepository;
import org.bson.Document;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeleteOrderRoute extends RouteBuilder {
    private final RedisTemplate<String, String> redisTemplate;

    DeleteOrderRoute(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void configure() throws Exception{
        errorHandler(deadLetterChannel(dlqEndpoint())
            .maximumRedeliveries(3)
            .redeliveryDelay(5000)
            .maximumRedeliveryDelay(60000)
            .useExponentialBackOff()
            .backOffMultiplier(2)
            .retryAttemptedLogLevel(LoggingLevel.WARN));

        SpringRedisIdempotentRepository redisIdRepo = new SpringRedisIdempotentRepository(redisTemplate, "check-duplicate");

        from("direct:delete-order")
        .routeId("delete-order")
        .idempotentConsumer(header("X-Correlation-Id"), redisIdRepo)
        .skipDuplicate(true)
        .log("Received request for delete")
        // .process(exchange -> {
        //     Object body = exchange.getIn().getBody();

        //     if (body instanceof Document document) {
        //         String orderId = document.getString("orderId");
        //         exchange.getIn().setHeader("orderId", orderId);
        //     }
        // })

        // .setHeader(
        //     "orderId",
        //     simple("${header.orderId}")
        // )
        .to("kafka:{{kafka.topic.delete-order}}?brokers={{kafka.bootstrap-servers}}"
            + "&securityProtocol={{kafka.security-protocol}}"
            + "&saslMechanism={{kafka.sasl-mechanism}}"
            + "&saslJaasConfig={{kafka.sasl.jaas.config}}"
            + "&sslTruststoreLocation={{kafka.ssl-truststore-location}}"
            + "&sslTruststorePassword={{kafka.ssl-truststore-password}}"
            + "&sslTruststoreType={{kafka.ssl-truststore-type}}");
    }
    
    private String dlqEndpoint() {
        return "kafka:{{kafka.topic.delete-order-dlq}}?brokers={{kafka.bootstrap-servers}}"
            + "&securityProtocol={{kafka.security-protocol}}"
            + "&saslMechanism={{kafka.sasl-mechanism}}"
            + "&saslJaasConfig={{kafka.sasl.jaas.config}}"
            + "&sslTruststoreLocation={{kafka.ssl-truststore-location}}"
            + "&sslTruststorePassword={{kafka.ssl-truststore-password}}";
    }
}
