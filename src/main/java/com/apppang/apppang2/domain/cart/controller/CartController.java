package com.apppang.apppang2.domain.cart.controller;

import com.apppang.apppang2.domain.cart.dto.response.CartResponse;
import com.apppang.apppang2.domain.cart.service.CartService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CART")
@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "장바구니 조회")
    @GetMapping("/api/cart/items")
    public ApiResponse<CartResponse> getCartItems(Authentication authentication){

        Long userId = Long.parseLong(authentication.getName());

        CartResponse response = cartService.getCartItems(userId);

        return ApiResponse.success("장바구니 조회에 성공했습니다.", response);
    }
}