package com.apppang.apppang2;

import com.apppang.apppang2.domain.address.repository.AddressRepository;
import com.apppang.apppang2.domain.order.entity.OrderDetail;
import com.apppang.apppang2.domain.order.repository.OrderDetailRepository;
import com.apppang.apppang2.domain.order.repository.OrderRepository;
import com.apppang.apppang2.domain.payment.repository.PaymentRepository;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Payment 도메인 시나리오 테스트

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderPaymentScenarioTest extends ScenarioTestSupport {

    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ProductRepository productRepository;

    private static final long PRODUCT_ID_2 = 2L;
    private static final long PRODUCT_ID_3 = 3L;

    private final String mainEmail = uniqueEmail("scenario.order.main");
    private final String otherEmail = uniqueEmail("scenario.order.other");

    private String mainAccessToken;
    private String otherAccessToken;
    private Long mainUserId;
    private Long otherUserId;

    private Long mainAddressId;
    private Long otherAddressId;

    private Long mainOrderId;
    private Long otherOrderId;
    private int productStockAfterMainOrder;

    private final List<Long> createdOrderIds = new ArrayList<>();

    @Test
    @Order(1)
    @DisplayName("[준비] 메인/보조 유저 회원가입, 로그인, 배송지 등록")
    void setup_usersAndAddresses() throws Exception {
        signup(mainEmail);
        mainAccessToken = login(mainEmail);
        mainUserId = extractUserIdFromEmail(mainEmail);

        signup(otherEmail);
        otherAccessToken = login(otherEmail);
        otherUserId = extractUserIdFromEmail(otherEmail);

        mainAddressId = addAddress(mainAccessToken, "메인유저");
        otherAddressId = addAddress(otherAccessToken, "보조유저");

        Assertions.assertNotNull(mainAddressId);
        Assertions.assertNotNull(otherAddressId);

        // 등록 순서 = [주소 정리(main), 주소 정리(other), 주문/재고 복구] 이고
        // 실행은 역순(LIFO)이므로: 주문/재고 복구 -> 주소 삭제 -> (부모가) 유저 삭제
        // 순서로 실행되어 FK 위반 없이 안전하게 정리됨.
        registerCleanup(() -> addressRepository.findByUserId(mainUserId).forEach(addressRepository::delete));
        registerCleanup(() -> addressRepository.findByUserId(otherUserId).forEach(addressRepository::delete));
        registerCleanup(this::restoreStockAndDeleteOrders);
    }

    @Test
    @Order(2)
    @DisplayName("[주문생성] 정상 주문 생성 시 재고가 정확히 주문 수량만큼만 차감된다")
    void order_create_success() throws Exception {
        int stockBefore = getProductStock(PRODUCT_ID_2);

        Map<String, Object> item = Map.of("productId", PRODUCT_ID_2, "quantity", 1);
        Map<String, Object> body = Map.of("addressId", mainAddressId, "items", List.of(item));

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + mainAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        mainOrderId = extractData(result).path("orderId").asLong();
        createdOrderIds.add(mainOrderId);

        productStockAfterMainOrder = getProductStock(PRODUCT_ID_2);
        Assertions.assertEquals(stockBefore - 1, productStockAfterMainOrder,
                "주문 생성 시 재고가 정확히 주문 수량(1개)만큼만 차감되어야 합니다.");
    }

    @Test
    @Order(3)
    @DisplayName("[결제] 정상 결제 - 재고는 추가로 차감되지 않고 주문 상태만 PREPARING으로 변경된다")
    void payment_process_success() throws Exception {
        Map<String, Object> body = Map.of(
                "orderId", mainOrderId,
                "paymentMethod", "CARD",
                "isFromCart", false
        );

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + mainAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentStatus").value("SUCCESS"));

        com.apppang.apppang2.domain.order.entity.Order order =
                orderRepository.findById(mainOrderId).orElseThrow();
        Assertions.assertEquals("PREPARING", order.getOrderStatus().name());

        int stockAfterPayment = getProductStock(PRODUCT_ID_2);
        Assertions.assertEquals(productStockAfterMainOrder, stockAfterPayment,
                "결제 단계에서는 재고가 추가로 차감되면 안 됩니다.");
    }

    @Test
    @Order(4)
    @DisplayName("[결제] 이미 결제완료된 주문 재결제 시도 -> 400")
    void payment_alreadyPREPARING() throws Exception {
        Map<String, Object> body = Map.of(
                "orderId", mainOrderId,
                "paymentMethod", "CARD",
                "isFromCart", false
        );

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + mainAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("[결제] 본인 주문이 아닌 주문 결제 시도 -> 403")
    void payment_otherUser_forbidden() throws Exception {
        Map<String, Object> item = Map.of("productId", PRODUCT_ID_3, "quantity", 1);
        Map<String, Object> orderBody = Map.of("addressId", otherAddressId, "items", List.of(item));

        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + otherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderBody)))
                .andExpect(status().isOk())
                .andReturn();

        otherOrderId = extractData(orderResult).path("orderId").asLong();
        createdOrderIds.add(otherOrderId);

        Map<String, Object> paymentBody = Map.of(
                "orderId", otherOrderId,
                "paymentMethod", "CARD",
                "isFromCart", false
        );

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + mainAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentBody)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    @DisplayName("[결제] 존재하지 않는 주문 결제 시도 -> 404")
    void payment_nonExistentOrder_notFound() throws Exception {
        Map<String, Object> body = Map.of(
                "orderId", NON_EXISTENT_ORDER_ID,
                "paymentMethod", "CARD",
                "isFromCart", false
        );

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + mainAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    // ===== 정리(cleanup) 헬퍼 =====

    // 생성된 모든 주문의 재고를 복구하고 관련 데이터를 삭제
    private void restoreStockAndDeleteOrders() {
        for (Long orderId : createdOrderIds) {
            List<OrderDetail> details = orderDetailRepository.findByOrderId(orderId);

            for (OrderDetail detail : details) {
                productRepository.findById(detail.getProduct().getId()).ifPresent(product -> {
                    product.increaseStock(detail.getQuantity());
                    productRepository.save(product);
                });
            }

            paymentRepository.findByOrderId(orderId).ifPresent(paymentRepository::delete);
            details.forEach(orderDetailRepository::delete);
            orderRepository.findById(orderId).ifPresent(orderRepository::delete);
        }
    }

    // ===== 요청 헬퍼 =====

    private Long addAddress(String accessToken, String receiverName) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("receiver", receiverName);
        body.put("receiverPhone", "010-1234-5678");
        body.put("roadAddress", "서울시 테스트구 테스트로");
        body.put("detailAddress", "1층");
        body.put("isDefault", true);

        MvcResult result = mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        return extractData(result).asLong();
    }

    private int getProductStock(long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalStateException("테스트용 상품을 찾을 수 없습니다: " + productId))
                .getStock();
    }
}