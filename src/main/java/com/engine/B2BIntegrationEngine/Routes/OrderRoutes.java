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
        .skipDuplicate(true)
        .log("Processing now")
        // **IDEMPOTENCY USING EXCHANGE**
        // .process(exchange -> {
        //     String id = exchange.getIn().getHeader("X-Correlation-Id", String.class);
        //     Boolean isDuplicate = exchange.getProperty(Exchange.DUPLICATE_MESSAGE, Boolean.class);
        //     System.out.println("Duplicate? " + isDuplicate);
        // })
        // .end()
        // **IDEMPOTENCY USING setProperty**
        // .setProperty("isDuplicate", exchangeProperty(Exchange.DUPLICATE_MESSAGE))
        // .log("Duplicate? ${exchangeProperty.isDuplicate}")

        // ** INITIAL CAMEL VALIDATION **
        // .to("direct:validate-order")

        .to("mongodb:myMongoClient?database=B2B&collection=Orders&operation=insert");
        
        //initial validation using camel choice and simple expression
        from("direct:validate-order")
        .choice()
        .when(simple("${isEmpty(${body.partnerId})}"))
            .log("partnerId is empty")
        .when(simple("${isEmpty(${body.orderId})}"))
            .log("orderId is empty")
        .when(simple("${isEmpty(${body.timestamp})}"))
            .log("timestamp is empty")
        .when(simple("${isEmpty(${body.correlationId})}"))
            .log("orderId is correlationId");

    }
}