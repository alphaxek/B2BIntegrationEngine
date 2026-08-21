package com.engine.B2BIntegrationEngine.Routes;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.redis.processor.idempotent.SpringRedisIdempotentRepository;

@Component
public class OrderRoutes extends RouteBuilder{
    private final RedisTemplate<String, String> redisTemplate;

    OrderRoutes(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void configure() throws Exception {
        SpringRedisIdempotentRepository redisIdRepo = new SpringRedisIdempotentRepository(redisTemplate, "check-duplicate");
    

        from("direct:create-order")
        .routeId("create-order")
        .idempotentConsumer(header("X-Correlation-Id"), redisIdRepo)
        .skipDuplicate(false)
        .log("Processing now")
        .process(exchange -> {
            String id = exchange.getIn().getHeader("X-Correlation-Id", String.class);
            Boolean isDuplicate = exchange.getProperty(Exchange.DUPLICATE_MESSAGE, Boolean.class);
            System.out.println("Duplicate? " + isDuplicate);
            // log(isDuplicate);
            // log("Order is recieved ${body}");
            // to("direct:validate-order");
        })
        .end();
        

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