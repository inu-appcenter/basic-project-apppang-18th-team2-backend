package com.apppang.apppang2.domain.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderResponse {
    private Long orderId;
    private LocalDateTime orderedAt;
    private String orderStatus;      //OrderStatus enum 값 그대로 사용
    private String paymentStatus;    //Payment.status, 없으면 "UNPAID"로 처리
    private int totalPrice;
    private String thumbnail;
    private String productName;
    private int itemCount;
}