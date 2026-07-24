package com.apppang.apppang2.domain.order.controller;

import com.apppang.apppang2.domain.order.dto.request.CreateOrderRequest;
import com.apppang.apppang2.domain.order.dto.response.CreateOrderResponse;
import com.apppang.apppang2.domain.order.service.OrderService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ORDER")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성")
    @PostMapping("/api/orders")
    public ApiResponse<CreateOrderResponse> createOrder(Authentication authentication,
                                                        @Valid @RequestBody CreateOrderRequest request){
        Long userId = Long.parseLong(authentication.getName());

        CreateOrderResponse response = orderService.createOrder(userId, request);

        return ApiResponse.success("주문이 생성되었습니다.", response);
    }
}