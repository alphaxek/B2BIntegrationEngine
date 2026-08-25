package com.engine.B2BIntegrationEngine.Routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class AsyncCreateOrderRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
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
        // .to("mongodb:myMongoClient?database=B2B&collection=Orders&operation=insert");
    }
}
