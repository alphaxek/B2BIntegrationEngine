package com.engine.B2BIntegrationEngine.Routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import org.apache.camel.LoggingLevel;

@Component
public class AsyncCreateOrderRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        errorHandler(deadLetterChannel(dlqEndpoint())
            .maximumRedeliveries(3)
            .redeliveryDelay(5000)
            .maximumRedeliveryDelay(60000)
            .useExponentialBackOff()
            .backOffMultiplier(2)
            .retryAttemptedLogLevel(LoggingLevel.WARN));

        from("kafka:{{kafka.topic.create-order}}?brokers={{kafka.bootstrap-servers}}"
            + "&securityProtocol={{kafka.security-protocol}}"
            + "&saslMechanism={{kafka.sasl-mechanism}}"
            + "&saslJaasConfig={{kafka.sasl.jaas.config}}"
            + "&sslTruststoreLocation={{kafka.ssl-truststore-location}}"
            + "&sslTruststorePassword={{kafka.ssl-truststore-password}}"
            + "&sslTruststoreType={{kafka.ssl-truststore-type}}")
        .routeId("create-order-async")
        .log("Received message from Kafka: ${body}")
        .log("storing order in MongoDB")
        .to("mongodb:myMongoClient?database=B2B&collection=Orders&operation=insert");
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
