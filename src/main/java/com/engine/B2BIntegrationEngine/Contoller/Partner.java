package com.engine.B2BIntegrationEngine.Contoller;

import org.springframework.web.bind.annotation.RestController;

import com.engine.B2BIntegrationEngine.Model.PartnerOrder;
import com.engine.B2BIntegrationEngine.Service.OrderService;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;



@RestController()
@RequestMapping("/partner")
public class Partner {

    @Autowired
    private OrderService service;

    private final ProducerTemplate producerTemplate;


    Partner(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }


    @GetMapping("/orders")
    public String getOrder() {
        return service.getOrders();
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
