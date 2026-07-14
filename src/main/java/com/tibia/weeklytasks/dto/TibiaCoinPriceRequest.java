package com.tibia.weeklytasks.dto;

public class TibiaCoinPriceRequest {
    private Integer quantity;

    public TibiaCoinPriceRequest() {
    }

    public TibiaCoinPriceRequest(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
