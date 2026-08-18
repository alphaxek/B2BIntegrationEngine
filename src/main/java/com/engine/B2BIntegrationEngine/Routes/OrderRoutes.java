package com.engine.B2BIntegrationEngine.Routes;

import org.springframework.stereotype.Component;
import org.apache.camel.builder.RouteBuilder;

@Component
public class OrderRoutes extends RouteBuilder{
    @Override
    public void configure() throws Exception {
        from("direct:create-order")
        .routeId("create-order")
        .log("Order is recieved ${body}");
    }
}