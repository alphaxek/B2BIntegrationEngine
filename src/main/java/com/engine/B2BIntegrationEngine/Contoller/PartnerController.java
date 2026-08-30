package com.engine.B2BIntegrationEngine.Contoller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.engine.B2BIntegrationEngine.Model.PartnerOrder;
import com.engine.B2BIntegrationEngine.Service.OrderService;

import io.micrometer.core.ipc.http.HttpSender.Response;

import java.net.URI;

import org.apache.camel.ProducerTemplate;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;



@RestController()
@RequestMapping("/partner")
public class PartnerController {

    @Autowired
    private OrderService service;
    Logger logger = LoggerFactory.getLogger(PartnerController.class);

    private final ProducerTemplate producerTemplate;


    PartnerController(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }


    @GetMapping(
        value = "/orders/{orderId}",
        produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> getOrder(@PathVariable String orderId) {
        logger.info("Received request to get order with ID: {}", orderId);
        Object result = producerTemplate.requestBodyAndHeader(
            "direct:get-order", 
            orderId, 
            "orderId",
            orderId
        );
        
        if(result == null) {
            return ResponseEntity.notFound().build();
        }else{
            return ResponseEntity.ok(result);
        }
    }

    @PostMapping(
        value = "/orders", 
        consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<Object> createOrder(
        @Valid @RequestBody PartnerOrder partnerOrder,
        @RequestHeader(value = "X-Correlation-Id", required=false) String correlationId ) {
        
        //route request to camel route
        producerTemplate.sendBodyAndHeader(
            "direct:create-order", 
            partnerOrder, 
            "X-Correlation-Id", 
            correlationId
        );

        //prepare the href
        URI href = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{orderId}")
            .buildAndExpand(partnerOrder.getOrderId())
            .toUri();

        //prepare response and return
       return ResponseEntity
       .accepted()
       .location(href)
       .body(java.util.Map.of(
            "message", "Order creation request accepted",
            "orderId", partnerOrder.getOrderId(),
            "href", href.toString()
       ));
    }
    
    @PatchMapping(
        value = "/orders",
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public ResponseEntity<Object> updateOrder(
        @RequestHeader("X-Correlation-Id") String correlationId,
        @Valid @RequestBody PartnerOrder partnerOrder
    ){
        Object result = producerTemplate.requestBodyAndHeader(
            "direct:update-order",
            partnerOrder,
            "X-Correlation-Id",
            correlationId
        );

        if(result == null){
            return ResponseEntity.notFound().build();
        }else{
            return ResponseEntity.ok(result);
        }
    }

    @DeleteMapping(
        value = "/orders/{orderId}",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public ResponseEntity<Object> deleteOrder(
        @PathVariable String orderId,
        @RequestHeader("X-Correlation-Id") String correlationId
    ){
        Object result = producerTemplate.requestBodyAndHeader(
            "direct:delete-order", 
            new Document("orderId", orderId), 
            "X-Correlation-Id",
            correlationId
        );

        if(result == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}
