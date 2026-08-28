package com.engine.B2BIntegrationEngine.Routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.springframework.stereotype.Component;

@Component
public class GetOrderRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception{
        from("direct:get-order")
        .routeId("get-order")
        .log("Received request to get order with ID: ${header.orderId}")

        .setHeader(
            MongoDbConstants.CRITERIA,
            simple("{\"orderId\": \"${header.orderId}\"}")
        )

        .to("mongodb:myMongoClient?database=B2B&collection=Orders&operation=findOneByQuery")

        .choice()
            .when(body().isNull())
                .log("Order with ID ${header.orderId} not found")
                .setHeader("CamelHttpResponseCode", constant(404))
                .setBody(constant("Order not found"))
            .otherwise()
                .log("Order with ID ${header.orderId} found: ${body}")
                .setHeader("CamelHttpResponseCode", constant(200))
        .end();
    }
}
