package com.apppang.apppang2.domain.order.dto.response;

import lombok.Getter;

@Getter
public class CreateOrderResponse {
    private Long orderId;
    private int totalPrice;

    public CreateOrderResponse(Long orderId, int totalPrice){
        this.orderId = orderId;
        this.totalPrice = totalPrice;
    }
}