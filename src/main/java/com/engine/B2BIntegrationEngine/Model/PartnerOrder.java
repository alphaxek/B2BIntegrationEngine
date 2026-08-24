package com.engine.B2BIntegrationEngine.Model;

import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Component;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;

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
    @Pattern(regexp = "^[A-Z]{2}\\d{4}$")
    private String orderId;
    @Nonnull
    private OrderType orderType;
    private OrderStatus orderStatus;
    private Date timestamp; 
    private String currency;
    private BigDecimal totalAmount;
    @Valid
    private List<Item> items;
    private String shipToAddress;

    public PartnerOrder(){

    }

    public PartnerOrder(
            int partnerId,
            String orderId,
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

    public void setOrderId(String orderId){
        this.orderId = orderId;
    }

    public String getOrderId(){
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

    public void setTotalAmount(BigDecimal totalAmount){
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalAmount(){
        return this.totalAmount;
    }

    public void setShipToAddress(String shipToAddress){
        this.shipToAddress = shipToAddress;
    }

    public String getShipToAddress(){
        return this.shipToAddress;
    }

    public void setCorrelationId(int correlationId){
        this.correlationId = correlationId;
    }

    public int getCorrelationId(){
        return this.correlationId;
    }

    @AssertTrue(message = "ORDER must contain items and totalAmount must equal the item total")
    public boolean isOrderTotalValid(){
        if (orderType != OrderType.ORDER) {
            return true;
        }
        if (items == null || items.isEmpty() || totalAmount == null) {
            return false;
        }

        BigDecimal itemTotal = BigDecimal.ZERO;
        for (Item item : items) {
            if (item == null || item.getQuantity() <= 0 || item.getUnitPrice() == null
                    || item.getUnitPrice().signum() <= 0) {
                return false;
            }
            itemTotal = itemTotal.add(
                    item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return totalAmount.compareTo(itemTotal) == 0;
    }

    @AssertTrue(message = "SHIPMENT_NOTICE must contain shipToAddress")
    public boolean isShipmentAddressValid(){
        return orderType != OrderType.SHIPMENT_NOTICE
                || (shipToAddress != null && !shipToAddress.isBlank());
    }

    @AssertTrue(message = "timestamp must not be in the future")
    public boolean isTimestampValid(){
        return timestamp == null || !timestamp.after(new Date());
    }

    public String toString(){
        return this.partnerId+" "+this.orderId+" "+this.items+" "+this.timestamp+" "+this.correlationId;
    }
}