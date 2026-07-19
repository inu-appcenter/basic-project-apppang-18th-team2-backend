package com.apppang.apppang2.domain.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "배송지를 선택해주세요.")
    private Long addressId;     //등록된 배송지 중 선택

    @Valid
    @NotEmpty(message = "주문 상품이 없습니다.")
    private List<OrderItemRequest> items;
}