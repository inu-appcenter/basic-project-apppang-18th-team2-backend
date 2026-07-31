package com.apppang.apppang2.order;

import com.apppang.apppang2.domain.address.entity.Address;
import com.apppang.apppang2.domain.address.repository.AddressRepository;
import com.apppang.apppang2.domain.cart.entity.Cart;
import com.apppang.apppang2.domain.cart.repository.CartRepository;
import com.apppang.apppang2.domain.category.entity.Category;
import com.apppang.apppang2.domain.category.repository.CategoryRepository;
import com.apppang.apppang2.domain.order.dto.request.CreateOrderRequest;
import com.apppang.apppang2.domain.order.dto.request.OrderItemRequest;
import com.apppang.apppang2.domain.order.entity.Order;
import com.apppang.apppang2.domain.order.entity.OrderDetail;
import com.apppang.apppang2.domain.order.entity.OrderStatus;
import com.apppang.apppang2.domain.order.repository.OrderDetailRepository;
import com.apppang.apppang2.domain.order.repository.OrderRepository;
import com.apppang.apppang2.domain.payment.entity.Payment;
import com.apppang.apppang2.domain.payment.entity.PaymentMethod;
import com.apppang.apppang2.domain.payment.entity.PaymentStatus;
import com.apppang.apppang2.domain.payment.repository.PaymentRepository;
import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import com.apppang.apppang2.domain.user.entity.Role;
import com.apppang.apppang2.domain.user.entity.User;
import com.apppang.apppang2.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderTest{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private User savedUser;
    private Product savedProduct;
    private Address savedAddress;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
        orderRepository.deleteAll();
        orderDetailRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        cartRepository.deleteAll();
        userRepository.deleteAll();

        savedUser = userRepository.save(User.builder()
                .email("test@test.com")
                .name("홍길동")
                .password("password1234")
                .phone("01012345678")
                .role(Role.USER)
                .build());

        Category category = Category.builder()
                .id(1L)
                .name("테스트 카테고리")
                .build();
        Category savedCategory = categoryRepository.save(category);

        savedProduct = productRepository.save(Product.builder()
                .category(savedCategory)
                .name("통합테스트 상품")
                .description("테스트 상품 설명입니다.")
                .price(10000)
                .salePrice(9000)
                .stock(10)
                .image1("test.jpg")
                .image2("test2.jpg")
                .build());

        savedAddress = addressRepository.save(Address.builder()
                .user(savedUser)
                .receiver("홍길동")
                .receiverPhone("01012345678")
                .roadAddress("테스트로 123")
                .detailAddress("101호")
                .build());
    }

    @Test
    @DisplayName("주문 생성 성공 - 주문 시 상품 재고가 정상적으로 차감")
    @WithMockUser(username = "1")
    void createOrder_stock_decrease_success() throws Exception {
        int initialStock = savedProduct.getStock(); // 10개
        int orderQuantity = 3;

        OrderItemRequest itemRequest = new OrderItemRequest();
        ReflectionTestUtils.setField(itemRequest, "productId", savedProduct.getId());
        ReflectionTestUtils.setField(itemRequest, "quantity", orderQuantity);

        CreateOrderRequest request = new CreateOrderRequest();
        ReflectionTestUtils.setField(request, "addressId", savedAddress.getId());
        ReflectionTestUtils.setField(request, "items", List.of(itemRequest));

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andDo(print());

        // 재고 검증: 10개에서 3개가 빠져서 7개가 되어야 함
        Product updatedProduct = productRepository.findById(savedProduct.getId()).get();
        assertThat(updatedProduct.getStock()).isEqualTo(initialStock - orderQuantity);
    }

    @Test
    @DisplayName("주문 목록 조회 성공 - 내 주문 목록 조회")
    @WithMockUser(username = "1")
    void getMyOrders_success() throws Exception {
        //given: 미리 주문 데이터 하나 생성해두기
        Order order = orderRepository.save(Order.builder()
                .userId(savedUser.getId())
                .totalPrice(9000)
                .orderStatus(OrderStatus.DELIVERING)
                .paymentMethod(PaymentMethod.CARD)
                .receiver("홍길동")
                .phone("01012345678")
                .address("테스트로 123")
                .build());

        orderDetailRepository.save(OrderDetail.builder()
                .order(order)
                .product(savedProduct)
                .quantity(1)
                .price(10000)
                .discountPrice(9000)
                .build());

        //when & then
        mockMvc.perform(get("/api/orders")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orders").isArray())
                .andExpect(jsonPath("$.data.orders[0].orderId").value(order.getId()))
                .andDo(print());
    }

    @Test
    @DisplayName("주문 상세 조회 성공 - 주문 상세 및 전화번호 마스킹 확인")
    @WithMockUser(username = "1")
    void getOrderDetail_success() throws Exception {
        Order savedOrder = orderRepository.save(Order.builder()
                .userId(savedUser.getId())
                .totalPrice(9000)
                .orderStatus(OrderStatus.DELIVERING)
                .paymentMethod(PaymentMethod.CARD)
                .receiver("홍길동")
                .phone("01012345678")
                .address("테스트로 123")
                .detailAddress("101호")
                .build());

        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.SUCCESS)
                .build();
        paymentRepository.save(payment);

        orderDetailRepository.save(OrderDetail.builder()
                .order(savedOrder)
                .product(savedProduct)
                .quantity(1)
                .price(10000)
                .discountPrice(9000)
                .build());

        mockMvc.perform(get("/api/orders/{orderId}", savedOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(savedOrder.getId()))
                .andExpect(jsonPath("$.data.receiver.phone").value("010-****-5678"))
                .andExpect(jsonPath("$.data.payment.paymentMethod").value("CARD"))
                .andExpect(jsonPath("$.data.items[0].productName").value("통합테스트 상품"))
                .andDo(print());
    }

    @Test
    @DisplayName("주문 취소 성공 - 주문 취소 시 상태 변경 및 재고 복구")
    @WithMockUser(username = "1")
    void cancelOrder_success() throws Exception {
        int initialStock = savedProduct.getStock(); // 10개
        int orderQuantity = 2;

        // 주문 당시 재고 미리 차감 가정
        savedProduct.decreaseStock(orderQuantity);
        productRepository.save(savedProduct);

        Order order = orderRepository.save(Order.builder()
                .userId(savedUser.getId())
                .totalPrice(18000)
                .orderStatus(OrderStatus.DELIVERING)
                .paymentMethod(PaymentMethod.CARD)
                .receiver("홍길동")
                .phone("01012345678")
                .address("테스트로 123")
                .build());

        orderDetailRepository.save(OrderDetail.builder()
                .order(order)
                .product(savedProduct)
                .quantity(orderQuantity)
                .price(10000)
                .discountPrice(9000)
                .build());

        mockMvc.perform(patch("/api/orders/{orderId}/cancel", order.getId()))
                .andExpect(status().isOk())
                .andDo(print());

        // 재고 복구 검증 (8개 -> 10개로 복구)
        Product recoveredProduct = productRepository.findById(savedProduct.getId()).get();
        assertThat(recoveredProduct.getStock()).isEqualTo(initialStock);
    }

    @Test
    @DisplayName("배송 조회 성공 - 배송 중인 주문의 배송 정보 조회")
    @WithMockUser(username = "1")
    void getDelivery_success() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .userId(savedUser.getId())
                .totalPrice(9000)
                .orderStatus(OrderStatus.DELIVERING)
                .paymentMethod(PaymentMethod.CARD)
                .receiver("홍길동")
                .phone("01012345678")
                .address("테스트로 123")
                .build());

        mockMvc.perform(get("/api/orders/{orderId}/delivery", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DELIVERING"))
                .andExpect(jsonPath("$.data.trackingNumber").value("1234567890"))
                .andExpect(jsonPath("$.data.deliveryCompany").value("CJ대한통운"))
                .andDo(print());
    }
}