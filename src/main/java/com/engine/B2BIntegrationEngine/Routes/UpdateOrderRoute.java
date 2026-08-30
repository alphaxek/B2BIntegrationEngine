package com.engine.B2BIntegrationEngine.Routes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.apache.camel.component.redis.processor.idempotent.SpringRedisIdempotentRepository;
import org.bson.Document;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.w3c.dom.ranges.DocumentRange;

import com.ctc.wstx.dtd.DFAState;
import com.engine.B2BIntegrationEngine.Model.Item;
import com.engine.B2BIntegrationEngine.Model.PartnerOrder;

@Component
public class UpdateOrderRoute extends RouteBuilder{
    private final RedisTemplate<String, String> redisTemplate;

    UpdateOrderRoute(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void configure() throws Exception {
        SpringRedisIdempotentRepository redisIdRepo = new SpringRedisIdempotentRepository(redisTemplate, "duplicate-check");

        from("direct:update-order")
        .routeId("update-order")
        .idempotentConsumer(header("X-Correlation-Id"), redisIdRepo)
        .skipDuplicate(true)
        .log("Received request to update")
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


            exchange.getIn().setBody(Arrays.asList(filter, updatedFields));
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
}
