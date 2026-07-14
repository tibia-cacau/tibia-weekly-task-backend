package com.tibia.weeklytasks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TibiaCoinPriceResponse {
    @JsonProperty("price")
    private String price;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("priceNumeric")
    private Double priceNumeric;

    @JsonProperty("total")
    private Double total;

    public TibiaCoinPriceResponse() {
    }

    public TibiaCoinPriceResponse(String price, Integer quantity, Double priceNumeric, Double total) {
        this.price = price;
        this.quantity = quantity;
        this.priceNumeric = priceNumeric;
        this.total = total;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
        // Extrair valor numérico do formato "R$18,05"
        if (price != null) {
            try {
                String numericValue = price.replace("R$", "").replace(",", ".").trim();
                this.priceNumeric = Double.parseDouble(numericValue);
            } catch (NumberFormatException e) {
                this.priceNumeric = 0.0;
            }
        }
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPriceNumeric() {
        return priceNumeric;
    }

    public void setPriceNumeric(Double priceNumeric) {
        this.priceNumeric = priceNumeric;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
}
