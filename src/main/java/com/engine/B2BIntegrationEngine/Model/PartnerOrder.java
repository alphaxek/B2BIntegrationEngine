package com.engine.B2BIntegrationEngine.Model;

import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Component;
import jakarta.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.*;

@Component
public class PartnerOrder{
    public enum OrderType {
        ORDER,
        INVOICE,
        SHIPMENT_NOTICE
    }

    public enum OrderStatus {
       RECEIVED,
       VALIDATED,
       PROCESSING,
       COMPLETED,
       FAILED,
       CANCELLED,
       VOIDED
    }

    @Id
    private int id;
    @Nonnull
    private int correlationId;
    @Nonnull
    private int partnerId;
    @Nonnull
    private int orderId;
    @Nonnull
    private OrderType orderType;
    private OrderStatus orderStatus;
    private Date timestamp; 
    private String currency;
    private BigDecimal totalAmount;
    private List<Item> items;
    private String shipToAddress;

    public PartnerOrder(){

    }

    public PartnerOrder(
            int partnerId,
            int orderId,
            List<Item> items,
            Date timestamp,
            int correlationId){
        this.partnerId = partnerId;
        this.orderId = orderId;
        this.items = items;
        this.timestamp = timestamp;
        this.correlationId = correlationId;
    }

    public void setPartnerId(int partnerId){
        this.partnerId = partnerId;
    }

    public int getPartnerId(){
        return this.partnerId;
    }

    public void setOrderId(int orderId){
        this.orderId = orderId;
    }

    public int getOrderId(){
        return this.orderId;
    }

    public void setOrderType(OrderType orderType){
        this.orderType = orderType;
    }

    public OrderType getOrderType(){
        return this.orderType;
    }

    public void setItems(List<Item> items){
        this.items = items;
    }

    public List<Item> getItems(){
        return this.items;
    }

    public void setTimestamp(Date timestamp){
        this.timestamp = timestamp;
    }

    public Date getTimestamp(){
        return this.timestamp;
    }

    public void setCorrelationId(int correlationId){
        this.correlationId = correlationId;
    }

    public int getCorrelationId(){
        return this.correlationId;
    }

    public String toString(){
        return this.partnerId+" "+this.orderId+" "+this.items+" "+this.timestamp+" "+this.correlationId;
    }
}