package com.engine.B2BIntegrationEngine.Contoller;

import org.springframework.web.bind.annotation.RestController;

import com.engine.B2BIntegrationEngine.Model.PartnerOrder;
import com.engine.B2BIntegrationEngine.Service.OrderService;

import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<String> createOrder(
        @Valid @RequestBody PartnerOrder partnerOrder,
        @RequestHeader(value = "X-Correlation-ID", required=false) String correlationId ) {
        
        producerTemplate.sendBodyAndHeader(
            "direct:create-order", 
            partnerOrder, 
            "X-Correlation-ID", 
            correlationId
        );

       return ResponseEntity.accepted().body("Order recieved");
    }
    
    
}
