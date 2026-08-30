package com.engine.B2BIntegrationEngine.Routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class AsyncDeleteOrder extends RouteBuilder {
    @Override
    public void configure() throws Exception{
        errorHandler(deadLetterChannel(dlqEndpoint())
            .maximumRedeliveries(3)
            .redeliveryDelay(5000)
            .maximumRedeliveryDelay(60000)
            .useExponentialBackOff()
            .backOffMultiplier(2)
            .retryAttemptedLogLevel(LoggingLevel.WARN));

        from("kafka:{{kafka.topic.delete-order}}?brokers={{kafka.bootstrap-servers}}"
            + "&securityProtocol={{kafka.security-protocol}}"
            + "&saslMechanism={{kafka.sasl-mechanism}}"
            + "&saslJaasConfig={{kafka.sasl.jaas.config}}"
            + "&sslTruststoreLocation={{kafka.ssl-truststore-location}}"
            + "&sslTruststorePassword={{kafka.ssl-truststore-password}}"
            + "&sslTruststoreType={{kafka.ssl-truststore-type}}")
        .routeId("delete-order-async")
        .log("Received message from Kafka for delete: ${body}")
        .log("Received message from Kafka for delete: ${header.orderId}")

        .process(exchange -> {
            Object body = exchange.getIn().getBody();

            String orderId = null;
            if (body instanceof Document doc) {
                orderId = doc.getString("orderId");
            } else if (body instanceof String s) {
                orderId = s;
            }

            if (orderId == null) {
                exchange.getIn().setBody(null);
                return;
            }

            // set header only for logging / downstream usage
            exchange.getIn().setHeader("orderId", orderId);

            // this is what MongoDB remove expects
            exchange.getIn().setBody(new Document("orderId", orderId));
        })

        .to("mongodb:myMongoClient?database=B2B&collection=Orders&operation=remove")

        .choice()
            .when(body().isNull())
                .log("Order with ID ${header.orderId} not found")
                .setHeader("CamelHttpResponseCode", constant(404))
                .setBody(constant("Order not found"))
            .otherwise()
                .log("Order with ID ${header.orderId} deleted: ${body}")
                .setHeader("CamelHttpResponseCode", constant(200))
        .end();
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
