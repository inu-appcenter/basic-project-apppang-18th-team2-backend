package com.apppang.apppang2.cart;

import com.apppang.apppang2.domain.cart.dto.request.AddCartItemRequest;
import com.apppang.apppang2.domain.cart.dto.request.UpdateCartQuantityRequest;
import com.apppang.apppang2.domain.cart.entity.Cart;
import com.apppang.apppang2.domain.cart.repository.CartRepository;
import com.apppang.apppang2.domain.category.entity.Category;
import com.apppang.apppang2.domain.category.repository.CategoryRepository;
import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CartTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;  //JSON 문자열로 변환

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product savedProduct;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp(){
        cartRepository.deleteAll();
        productRepository.deleteAll();

        // 카테고리는 매번 지우지 않고, 이미 있으면 그걸 재사용
        Category savedCategory = categoryRepository.findById(1L)
                .orElseGet(() -> categoryRepository.save(
                        Category.builder()
                                .id(1L)
                                .name("테스트 카테고리")
                                .build()
                ));

        Product product = Product.builder()
                .category(savedCategory)
                .name("통합테스트 상품")
                .description("테스트 상품 설명입니다.")
                .price(15000)
                .salePrice(13000)
                .stock(10)
                .image1("test.jpg")
                .image2("test2.jpg")    //상세이미지
                .build();
        savedProduct = productRepository.save(product);
    }

    @Test
    @DisplayName("장바구니 담기 성공")
    @WithMockUser(username = "1")   //userId를 1로 인식하게 하는 가짜 인증 유저 설정
    void addCartItem_success() throws Exception{
        //given
        AddCartItemRequest request = new AddCartItemRequest();
        //ReflectionTestUtils을 사용해 강제로 필드에 값을 주입
        ReflectionTestUtils.setField(request, "productId", savedProduct.getId());
        ReflectionTestUtils.setField(request,"quantity",2);

        String content = objectMapper.writeValueAsString(request);

        //when&then
        mockMvc.perform(post("/api/cart/items")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장바구니에 담았습니다."))
                .andDo(print());

        //DB에 장바구니 데이터가 잘 들어갔는지 최종 확인
        var carts = cartRepository.findAll();
        assertThat(carts).hasSize(1);
        assertThat(carts.get(0).getUserId()).isEqualTo(1L);
        assertThat(carts.get(0).getQuantity()).isEqualTo(2);
        assertThat(carts.get(0).getProduct().getId()).isEqualTo(savedProduct.getId());
    }

    @Test
    @DisplayName("장바구니 담기 실패 - 존재하지 않는 상품")
    @WithMockUser(username = "1")
    void addCartItem_productNotFound_fail() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest();
        ReflectionTestUtils.setField(request, "productId", 9999L);
        ReflectionTestUtils.setField(request,"quantity",1);

        String content = objectMapper.writeValueAsString(request);

        //when&then
        mockMvc.perform(post("/api/cart/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andDo(print());
    }

    @Test
    @DisplayName("장바구니 담기 실패 - 재고 초과")
    @WithMockUser(username = "1")
    void addCartItem_exeedStock_fail() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest();
        ReflectionTestUtils.setField(request, "productId", savedProduct.getId());
        ReflectionTestUtils.setField(request,"quantity",15);

        String content = objectMapper.writeValueAsString(request);

        //when&then
        mockMvc.perform(post("/api/cart/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("재고보다 많은 수량을 담을 수 없습니다."))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andDo(print());
    }

    @Test
    @DisplayName("장바구니 조회 성공")
    @WithMockUser(username = "1")
    void getCartItems_success() throws Exception{
        //given
        Cart cart = Cart.builder()
                .userId(1L)
                .product(savedProduct)
                .quantity(3)
                .build();
        cartRepository.save(cart);

        //when&then
        mockMvc.perform(get("/api/cart/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPrice").value(39000))
                .andExpect(jsonPath("$.data.items[0].productName").value("통합테스트 상품"))
                .andDo(print());
    }

    @Test
    @DisplayName("장바구니 수량 조절 성공")
    @WithMockUser(username = "1")
    void updateQuantity_success() throws Exception {
        //given: 장바구니에 1개 담긴 상태를 미리 구성
        Cart cart = Cart.builder()
                .userId(1L)
                .product(savedProduct)
                .quantity(1)
                .build();
        Cart savedCart = cartRepository.save(cart);

        //수량을 5개로 변경하겠다는 요청 데이터 생성
        UpdateCartQuantityRequest request = new UpdateCartQuantityRequest();
        ReflectionTestUtils.setField(request, "quantity", 5);
        String content = objectMapper.writeValueAsString(request);

        //when&then
        mockMvc.perform(patch("/api/cart/items/{cartItemId}", savedCart.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(5))   //응답 데이터에 수량이 5로 변경되었는지 확인
                .andDo(print());
    }

    @Test
    @DisplayName("장바구니 수량 조절 실패 - 존재하지 않는 장바구니 항목")
    @WithMockUser(username = "1")
    void updateQuantity_cartItemNotFound_fail() throws Exception {
        UpdateCartQuantityRequest request = new UpdateCartQuantityRequest();
        ReflectionTestUtils.setField(request,"quantity",2);

        String content = objectMapper.writeValueAsString(request);

        //when&then
        mockMvc.perform(patch("/api/cart/items/{cartItemId}", 9999L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("존재하지 않거나 접근할 수 없는 장바구니 항목입니다."))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andDo(print());
    }

    @Test
    @DisplayName("장바구니 수량 조절 실패 - 재고 초과")
    @WithMockUser(username = "1")
    void updateQuantity_exceedStock_fail() throws Exception {
        Cart cart = Cart.builder()
                .userId(1L)
                .product(savedProduct)
                .quantity(1)
                .build();
        Cart savedCart = cartRepository.save(cart);

        UpdateCartQuantityRequest request = new UpdateCartQuantityRequest();
        ReflectionTestUtils.setField(request, "quantity", 15); // 재고 초과

        String content = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/cart/items/{cartItemId}", savedCart.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest()) // 400
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("재고보다 많은 수량을 담을 수 없습니다."))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andDo(print());
    }

    @Test
    @DisplayName("장바구니 상품 삭제 성공")
    @WithMockUser(username = "1")
    void deleteCartItem_success() throws Exception {
        //given
        Cart cart = Cart.builder()
                .userId(1L)
                .product(savedProduct)
                .quantity(1)
                .build();
        Cart savedCart = cartRepository.save(cart);

        //when&then
        mockMvc.perform(delete("/api/cart/items/{cartItemId}", savedCart.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("장바구니 상품이 삭제되었습니다."))
                .andDo(print());

        //데이터가 완전히 삭제되었는지 검증
        assertThat(cartRepository.findById(savedCart.getId())).isEmpty();
    }

    @Test
    @DisplayName("장바구니 상품 삭제 실패 - 존재하지 않는 장바구니 항목")
    @WithMockUser(username = "1")
    void deleteCartItem_cartItemNotFound_fail() throws Exception {
        mockMvc.perform(delete("/api/cart/items/{cartItemId}", 99999L) // 존재하지 않는 ID
                        .with(csrf()))
                .andExpect(status().isNotFound()) // 404
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("존재하지 않거나 접근할 수 없는 장바구니 항목입니다."))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andDo(print());
    }
}
