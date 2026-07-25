package com.apppang.apppang2.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentResponse {
    private Long paymentId;
    private Long orderId;
    private int amount;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paidAt;

    @Builder
    public PaymentResponse(Long paymentId, Long orderId, int amount, String paymentMethod
                            ,String paymentStatus, LocalDateTime paidAt){
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paidAt = paidAt;
    }
}
