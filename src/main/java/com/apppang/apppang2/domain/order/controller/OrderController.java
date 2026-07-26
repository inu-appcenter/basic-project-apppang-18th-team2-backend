package com.apppang.apppang2.domain.order.controller;

import com.apppang.apppang2.domain.order.dto.request.CreateOrderRequest;
import com.apppang.apppang2.domain.order.dto.response.CreateOrderResponse;
import com.apppang.apppang2.domain.order.dto.response.OrderDetailResponse;
import com.apppang.apppang2.domain.order.dto.response.OrderListResponse;
import com.apppang.apppang2.domain.order.service.OrderService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "ORDER")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성")
    @PostMapping()
    public ApiResponse<CreateOrderResponse> createOrder(Authentication authentication,
                                                        @Valid @RequestBody CreateOrderRequest request){
        Long userId = Long.parseLong(authentication.getName());

        CreateOrderResponse response = orderService.createOrder(userId, request);

        return ApiResponse.success("주문이 생성되었습니다.", response);
    }

    //주문 목록 조회 API
    @Operation(summary = "주문 목록 조회", description = "로그인한 유저의 주문 내역을 최신 순으로 정렬하여 조회합니다.")
    @GetMapping()
    public ApiResponse<OrderListResponse> getMyOrders(
            Authentication authentication,
            @Parameter(description = "조회할 페이지 번호(1부터 시작)", example = "0")
            @RequestParam(defaultValue = "1") int page) {
        Long userId = Long.parseLong(authentication.getName());

        OrderListResponse response = orderService.getMyOrders(userId, page-1); //DB에서는 0부터 시작이므로 -1값을 보냄
        return ApiResponse.success("주문 내역 조회에 성공했습니다.", response);
    }

    //주문 상세 조회 API
    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 내역을 조회합니다.")
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(
            Authentication authentication,
            @Parameter(description = "조회할 주문 ID", example = "105")
            @PathVariable Long orderId){
        
        //유저 아이디와 order아이디를 서비스로 넘김
        Long userId = Long.parseLong(authentication.getName());
        OrderDetailResponse response = orderService.getOrderDetail(userId, orderId);

        return ApiResponse.success("주문 상세 조회에 성공했습니다.", response);
    }
}