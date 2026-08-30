package com.engine.B2BIntegrationEngine.Routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.springframework.stereotype.Component;

@Component
public class DeleteOrderRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception{
        errorHandler(deadLetterChannel(dlqEndpoint())
            .maximumRedeliveries(3)
            .redeliveryDelay(5000)
            .maximumRedeliveryDelay(60000)
            .useExponentialBackOff()
            .backOffMultiplier(2)
            .retryAttemptedLogLevel(LoggingLevel.WARN));

        from("direct:delete-order")
        .routeId("delete-order")
        .log("Received request for delete")

        .setHeader(
            MongoDbConstants.CRITERIA,
            simple("{\"orderId\":\"${header.orderId}\"}")
        )
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
        return "kafka:{{kafka.topic.create-order-dlq}}?brokers={{kafka.bootstrap-servers}}"
            + "&securityProtocol={{kafka.security-protocol}}"
            + "&saslMechanism={{kafka.sasl-mechanism}}"
            + "&saslJaasConfig={{kafka.sasl.jaas.config}}"
            + "&sslTruststoreLocation={{kafka.ssl-truststore-location}}"
            + "&sslTruststorePassword={{kafka.ssl-truststore-password}}";
    }
}
