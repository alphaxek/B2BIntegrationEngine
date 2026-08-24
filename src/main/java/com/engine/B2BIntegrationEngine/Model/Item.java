package com.engine.B2BIntegrationEngine.Model;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import jakarta.validation.constraints.Positive;

@Component
public class Item {
    @Positive
    private String sku;
    @Positive
    private int quantity;
    @Positive
    private BigDecimal unitPrice;

    public Item() {
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}