package com.engine.B2BIntegrationEngine.Routes;

import org.springframework.stereotype.Component;
import org.apache.camel.builder.RouteBuilder;

@Component
public class OrderRoutes extends RouteBuilder{
    @Override
    public void configure() throws Exception {
        from("direct:create-order")
        .routeId("create-order")
        .log("Order is recieved ${body}")
        .to("direct:validate-order");

        from("direct:validate-order")
        .choice()
        .when(simple("${isEmpty(${body.partnerId})}"))
            .log("partnerId is empty")
        .when(simple("${isEmpty(${body.orderId})}"))
            .log("orderId is empty")
        .when(simple("${isEmpty(${body.items})}"))
            .log("items are empty")
        .when(simple("${isEmpty(${body.timestamp})}"))
            .log("timestamp is empty")
        .when(simple("${isEmpty(${body.correlationId})}"))
            .log("orderId is correlationId");

    }
}