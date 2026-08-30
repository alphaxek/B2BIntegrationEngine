package com.engine.B2BIntegrationEngine.Routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.springframework.stereotype.Component;

@Component
public class DeleteOrderRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception{
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
}
