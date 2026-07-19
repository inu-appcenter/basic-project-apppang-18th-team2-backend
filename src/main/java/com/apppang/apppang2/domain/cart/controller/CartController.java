package com.apppang.apppang2.domain.cart.controller;

import com.apppang.apppang2.domain.cart.dto.request.AddCartItemRequest;
import com.apppang.apppang2.domain.cart.dto.response.CartResponse;
import com.apppang.apppang2.domain.cart.service.CartService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Operation(summary = "장바구니 담기")
    @PostMapping("/api/cart/items")
    public ApiResponse<Void> addCartItem(Authentication authentication,
                                         @Valid @RequestBody AddCartItemRequest request){

        Long userId = Long.parseLong(authentication.getName());

        cartService.addCartItem(userId, request.getProductId(), request.getQuantity());

        return ApiResponse.success("장바구니에 담았습니다.");
    }
}