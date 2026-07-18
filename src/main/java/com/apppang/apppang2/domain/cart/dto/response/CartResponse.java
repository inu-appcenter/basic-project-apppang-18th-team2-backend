package com.apppang.apppang2.domain.cart.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class CartResponse {
    private List<CartItemResponse> items;
    private int totalPrice;

    public CartResponse(List<CartItemResponse> items, int totalPrice){
        this.items = items;
        this.totalPrice = totalPrice;
    }
}