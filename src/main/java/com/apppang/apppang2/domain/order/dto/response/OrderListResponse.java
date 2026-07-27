package com.apppang.apppang2.domain.order.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderListResponse {
    private List<OrderResponse> orders;
    private int page;
    private boolean hasNext;

    public OrderListResponse(List<OrderResponse> orders, int page, boolean hasNext){
        this.orders = orders;
        this.page = page;
        this.hasNext = hasNext;
    }
}