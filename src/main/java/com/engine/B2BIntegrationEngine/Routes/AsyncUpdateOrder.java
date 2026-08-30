package com.engine.B2BIntegrationEngine.Routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.bson.Document;
import org.springframework.stereotype.Component;

import com.engine.B2BIntegrationEngine.Model.PartnerOrder;

@Component
public class AsyncUpdateOrder extends RouteBuilder {
    @Override
    public void configure() throws Exception{
        errorHandler(deadLetterChannel(dlqEndpoint())
            .maximumRedeliveries(3)
            .redeliveryDelay(5000)
            .maximumRedeliveryDelay(60000)
            .useExponentialBackOff()
            .backOffMultiplier(2)
            .retryAttemptedLogLevel(LoggingLevel.WARN));

        from("kafka:{{kafka.topic.update-order}}?brokers={{kafka.bootstrap-servers}}"
            + "&securityProtocol={{kafka.security-protocol}}"
            + "&saslMechanism={{kafka.sasl-mechanism}}"
            + "&saslJaasConfig={{kafka.sasl.jaas.config}}"
            + "&sslTruststoreLocation={{kafka.ssl-truststore-location}}"
            + "&sslTruststorePassword={{kafka.ssl-truststore-password}}"
            + "&sslTruststoreType={{kafka.ssl-truststore-type}}")
        .routeId("update-order-async")
        .log("Received message from Kafka for update: ${body}")
        .unmarshal().json(PartnerOrder.class)
        .process(exchange -> {
            PartnerOrder partnerOrder = exchange.getIn().getBody(PartnerOrder.class);

            if(partnerOrder == null){
                exchange.getIn().setBody(null);
                return;
            }

            Document filter = new Document("orderId", partnerOrder.getOrderId());

            // List<Document> mongoItems = new ArrayList<>();
            // if (partnerOrder.getItems() != null) {
            //     for (Item item : partnerOrder.getItems()) {
            //         Document doc = new Document()
            //             .append("sku", item.getSku())
            //             .append("quantity", item.getQuantity())
            //             .append("unitPrice", item.getUnitPrice());

            //         mongoItems.add(doc);
            //     }
            // }

            Document updatedFields = new Document("$set", new Document()
                .append("partnerId", partnerOrder.getPartnerId())
                .append("orderId", partnerOrder.getOrderId())
                .append("orderType", partnerOrder.getOrderType())
                .append("orderStatus", partnerOrder.getOrderStatus())
                .append("timestamp", partnerOrder.getTimestamp())
                .append("totalAmount", partnerOrder.getTotalAmount())
                // .append("items", partnerOrder.getItems())
                .append("shipToAddress", partnerOrder.getShipToAddress())
                .append("correlationId", partnerOrder.getCorrelationId()));


            exchange.getIn().setHeader(MongoDbConstants.CRITERIA, filter);
            exchange.getIn().setBody(updatedFields);
        }

        )
        .to("mongodb:myMongoClient?database=B2B&collection=Orders&operation=update")

        .choice()
            .when(body().isNull())
                .setHeader("HttpCamelResponseCode", constant(404))
                .setBody(constant("No order found"))
            .otherwise()
                .setHeader("HttpCamelResponseCode", constant(200))
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
