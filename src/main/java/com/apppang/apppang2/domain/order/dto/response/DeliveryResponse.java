package com.apppang.apppang2.domain.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DeliveryResponse {
    private Long orderId;
    private String status;
    private String trackingNumber;
    private String deliveryCompany;
    private LocalDate estimatedArrival;
}