package com.apppang.apppang2.cart;

import com.apppang.apppang2.domain.cart.dto.response.CartQuantityResponse;
import com.apppang.apppang2.domain.cart.dto.response.CartResponse;
import com.apppang.apppang2.domain.cart.entity.Cart;
import com.apppang.apppang2.domain.cart.repository.CartRepository;
import com.apppang.apppang2.domain.cart.service.CartService;
import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import com.apppang.apppang2.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Test
    @DisplayName("장바구니 조회 성공")
    void getCartItems_success() {
        //given
        Long userId = 1L;

        Product product = Product.builder()
                .name("테스트 상품")
                .price(10000)
                .salePrice(9000)
                .stock(10)
                .image1("image.jpg")
                .build();

        Cart cart = Cart.builder()
                .userId(userId)
                .product(product)
                .quantity(2)
                .build();

        given(cartRepository.findAllWithProductByUserId(userId)).willReturn(List.of(cart));

        //when
        CartResponse response = cartService.getCartItems(userId);

        //then
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalPrice()).isEqualTo(18000);
    }

    @Test
    @DisplayName("장바구니 담기 성공 - 처음 담는 상품인 경우")
    void addCartItems_new_success() {
        //given
        Long userId = 1L;
        Long productId = 100L;
        int quantity = 2;

        Product product = Product.builder()
                .stock(10)
                .price(10000)
                .name("테스트 상품")
                .build();

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(cartRepository.findByUserIdAndProductId(userId, productId)).willReturn(Optional.empty());

        //when
        cartService.addCartItem(userId, productId, quantity);

        //then
        verify(cartRepository).save(any(Cart.class));
    }


    @Test
    @DisplayName("장바구니 담기 성공 - 이미 담겨 있는 상품인 경우 수량 증가")
    void addCartItem_existing_success() {
        //given
        Long userId = 1L;
        Long productId = 100L;
        int quantity = 2;

        Product product = Product.builder()
                .stock(10)
                .price(10000)
                .name("테스트 상품")
                .build();

        Cart existingCart = Cart.builder()
                .userId(userId)
                .product(product)
                .quantity(3)
                .build();

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(cartRepository.findByUserIdAndProductId(userId, productId)).willReturn(Optional.of(existingCart));

        //when
        cartService.addCartItem(userId, productId, quantity);

        //then
        assertThat(existingCart.getQuantity()).isEqualTo(5);
        verify(cartRepository, never()).save(any(Cart.class));   //save 대신 변경 감지
    }


    @Test
    @DisplayName("장바구니 담기 실패 - 재고보다 많은 수량을 요청한 경우")
    void addCartItem_exceedStock_fail() {
        //given
        Long userId = 1L;
        Long productId = 100L;
        int quantity = 16;      //재고 초과

        Product product = Product.builder()
                .stock(10)
                .price(10000)
                .name("테스트 상품")
                .build();

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(cartRepository.findByUserIdAndProductId(userId, productId)).willReturn(Optional.empty());


        assertThatThrownBy(() -> cartService.addCartItem(userId, productId, quantity))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(customEx.getMessage()).isEqualTo("재고보다 많은 수량을 담을 수 없습니다.");
                });
    }

    @Test
    @DisplayName("장바구니 수량 조절 성공")
    void updateQuantity_success() {
        //given
        Long userId = 1L;
        Long cartItemId = 1L;
        int newQuantity = 2;

        Product product = Product.builder()
                .stock(10)
                .price(10000)
                .name("테스트 상품")
                .build();

        Cart cart = Cart.builder()
                .userId(userId)
                .product(product)
                .quantity(3)
                .build();
        given(cartRepository.findByIdAndUserId(cartItemId, userId)).willReturn(Optional.of(cart));

        //when
        CartQuantityResponse response = cartService.updateQuantity(userId, cartItemId, newQuantity);

        //then
        assertThat(response.getQuantity()).isEqualTo(newQuantity);
        assertThat(cart.getQuantity()).isEqualTo(newQuantity);
    }

    @Test
    @DisplayName("장바구니 삭제 성공")
    void deleteCartItem_success() {
        //given
        Long userId = 1L;
        Long cartItemId = 1L;

        Product product = Product.builder().stock(10).build();
        Cart cart = Cart.builder().userId(userId).product(product).quantity(1).build();

        given(cartRepository.findByIdAndUserId(cartItemId, userId)).willReturn(Optional.of(cart));

        //when
        cartService.deleteCartItem(userId, cartItemId);

        //then
        verify(cartRepository).delete(cart);
    }
}