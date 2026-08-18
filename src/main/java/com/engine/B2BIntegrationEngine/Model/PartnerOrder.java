package com.engine.B2BIntegrationEngine.Model;

import org.springframework.stereotype.Component;
import jakarta.annotation.Nonnull;
import java.util.*;

@Component
public class PartnerOrder{
    @Nonnull
    private int partnerId;
    @Nonnull
    private int orderId;
    @Nonnull
    private int items[];
    private Date timestamp;
    private int correlationId;

    public PartnerOrder(){

    }

    public PartnerOrder(
            int partnerId,
            int orderId,
            int items[],
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

    public void setItems(int items[]){
        this.items = items;
    }

    public int[] getItems(){
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