package com.apppang.apppang2.domain.cart.service;

import com.apppang.apppang2.domain.cart.dto.response.CartItemResponse;
import com.apppang.apppang2.domain.cart.dto.response.CartResponse;
import com.apppang.apppang2.domain.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public CartResponse getCartItems(Long userId){
        List<CartItemResponse> items = cartRepository.findAllWithProductByUserId(userId).stream()
                .map(CartItemResponse::new)
                .toList();

        //총액 = 각 상품의 판매가 × 수량 합계
        int totalPrice = items.stream()
                .mapToInt(item -> item.getSalePrice() * item.getQuantity())
                .sum();

        return new CartResponse(items, totalPrice);
    }
}