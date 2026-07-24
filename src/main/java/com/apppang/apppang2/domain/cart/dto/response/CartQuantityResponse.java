package com.apppang.apppang2.domain.cart.dto.response;

import lombok.Getter;

@Getter
public class CartQuantityResponse {
    private int quantity;

    public CartQuantityResponse(int quantity){
        this.quantity = quantity;
    }
}