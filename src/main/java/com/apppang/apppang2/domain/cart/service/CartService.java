package com.apppang.apppang2.domain.cart.service;

import com.apppang.apppang2.domain.cart.dto.response.CartItemResponse;
import com.apppang.apppang2.domain.cart.dto.response.CartQuantityResponse;
import com.apppang.apppang2.domain.cart.dto.response.CartResponse;
import com.apppang.apppang2.domain.cart.entity.Cart;
import com.apppang.apppang2.domain.cart.repository.CartRepository;
import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import com.apppang.apppang2.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

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

    //장바구니 담기
    @Transactional
    public void addCartItem(Long userId, Long productId, int quantity){

        //1. 상품 존재 확인 (없으면 404)
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

        //2. 이미 담긴 상품인지 확인 없어도 정상처리
        Cart existing = cartRepository.findByUserIdAndProductId(userId, productId).orElse(null);

        //3. 장바구니에 담게 될 총 수량" (기존에 담아둔 것 + 요청)
        //상품의 남은 재고 보다 장바구니에 담는 수량이 재고를 넘을 수 없다는 검사
        int totalQuantity = (existing == null ? 0 : existing.getQuantity()) + quantity;
        if (totalQuantity > product.getStock()){
            throw new CustomException(HttpStatus.BAD_REQUEST, "재고보다 많은 수량을 담을 수 없습니다.");
        }

        //4. 이미 담겨 있으면 담은 수량만 증가, 처음이면 새 행 저장
        if (existing != null){
            existing.addQuantity(quantity);
        } else {
            cartRepository.save(Cart.builder()
                    .userId(userId)
                    .product(product)
                    .quantity(quantity)
                    .build());
        }
    }

    //수량 조절
    @Transactional
    public CartQuantityResponse updateQuantity(Long userId, Long cartItemId, int quantity){
        //내 장바구니 조회 — 없거나 남의 것이면 404
        Cart cart = cartRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

        //재고 검사
        if (quantity > cart.getProduct().getStock()){
            throw new CustomException(HttpStatus.BAD_REQUEST, "재고보다 많은 수량을 담을 수 없습니다.");
        }

        cart.updateQuantity(quantity);      //변경 감지로 UPDATE 자동 실행

        return new CartQuantityResponse(cart.getQuantity());
    }
}