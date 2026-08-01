package com.apppang.apppang2.order;

import com.apppang.apppang2.domain.address.entity.Address;
import com.apppang.apppang2.domain.address.repository.AddressRepository;
import com.apppang.apppang2.domain.order.dto.request.CreateOrderRequest;
import com.apppang.apppang2.domain.order.dto.request.OrderItemRequest;
import com.apppang.apppang2.domain.order.dto.response.CreateOrderResponse;
import com.apppang.apppang2.domain.order.dto.response.DeliveryResponse;
import com.apppang.apppang2.domain.order.dto.response.OrderDetailResponse;
import com.apppang.apppang2.domain.order.dto.response.OrderListResponse;
import com.apppang.apppang2.domain.order.entity.Order;
import com.apppang.apppang2.domain.order.entity.OrderDetail;
import com.apppang.apppang2.domain.order.entity.OrderStatus;
import com.apppang.apppang2.domain.order.repository.OrderDetailRepository;
import com.apppang.apppang2.domain.order.repository.OrderRepository;
import com.apppang.apppang2.domain.order.service.OrderService;
import com.apppang.apppang2.domain.payment.entity.Payment;
import com.apppang.apppang2.domain.payment.entity.PaymentMethod;
import com.apppang.apppang2.domain.payment.entity.PaymentStatus;
import com.apppang.apppang2.domain.payment.repository.PaymentRepository;
import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import com.apppang.apppang2.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("주문 성공 - 유효한 요청으로 주문을 생성하면 재고가 차감되고 주문이 저장")
    void createOrder_success() {
        //given
        Long userId = 1L;
        Long addressId = 1L;
        Long productId = 100L;
        int initialStock = 10;
        int orderQuantity = 2;
        int salePrice = 9000;

        CreateOrderRequest request = new CreateOrderRequest();
        ReflectionTestUtils.setField(request, "addressId", addressId);

        OrderItemRequest itemRequest = new OrderItemRequest();
        ReflectionTestUtils.setField(itemRequest, "productId", productId);
        ReflectionTestUtils.setField(itemRequest, "quantity", orderQuantity);
        ReflectionTestUtils.setField(request, "items", List.of(itemRequest));

        Address address = Address.builder()
                .receiver("홍길동")
                .receiverPhone("01012345678")
                .roadAddress("테스트시 테스트로 123")
                .detailAddress("101호")
                .build();

        Product product = Product.builder()
                .name("테스트상품")
                .price(10000)
                .salePrice(9000)
                .stock(initialStock)
                .image1("thumbnail.jpg")
                .build();
        ReflectionTestUtils.setField(product, "id", productId);

        Order savedOrder = Order.builder()
                .userId(userId)
                .totalPrice(salePrice * orderQuantity)
                .orderStatus(OrderStatus.PENDING)
                .paymentMethod(PaymentMethod.NONE)
                .receiver("홍길동")
                .phone("01012345678")
                .address("테스트시 테스트로 123")
                .detailAddress("101호")
                .build();
        ReflectionTestUtils.setField(savedOrder, "id", 1L);

        given(addressRepository.findByIdAndUserId(addressId, userId)).willReturn(Optional.of(address));
        given(productRepository.findAllByIdWithLock(any())).willReturn(List.of(product));
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

        //when
        CreateOrderResponse response = orderService.createOrder(userId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getTotalPrice()).isEqualTo(salePrice * orderQuantity);
        assertThat(product.getStock()).isEqualTo(initialStock - orderQuantity); // 재고 차감 확인

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderDetailRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("주문 실패 - 존재하지 않거나 남의 배송지면 예외 발생")
    void createOrder_address_not_found_fail() {
        //given
        Long userId = 1L;
        CreateOrderRequest request = new CreateOrderRequest();
        ReflectionTestUtils.setField(request, "addressId", 999L);

        given(addressRepository.findByIdAndUserId(999L, userId)).willReturn(Optional.empty());

        //when&then
        assertThatThrownBy(()->orderService.createOrder(userId,request))
                .isInstanceOf(CustomException.class)
                .satisfies(exception->{
                    CustomException ex = (CustomException) exception;
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getMessage()).isEqualTo("배송지를 찾을 수 없습니다.");
                });
    }

    @Test
    @DisplayName("주문 실패 - 주문 상품의 재고가 부족하면 예외 발생")
    void createOrder_insufficient_stock_fail(){
        //given
        Long userId = 1L;
        Long addressId = 1L;
        Long productId = 100L;

        CreateOrderRequest request = new CreateOrderRequest();
        ReflectionTestUtils.setField(request, "addressId", addressId);

        OrderItemRequest itemRequest = new OrderItemRequest();
        ReflectionTestUtils.setField(itemRequest, "productId", productId);
        ReflectionTestUtils.setField(itemRequest, "quantity", 5);
        ReflectionTestUtils.setField(request, "items", List.of(itemRequest));

        Address address = Address.builder()
                .receiver("홍길동")
                .receiverPhone("01012345678")
                .roadAddress("테스트시 테스트로 123")
                .detailAddress("101호")
                .build();

        Product product = Product.builder()
                .name("테스트상품")
                .price(10000)
                .stock(2)
                .image1("thumbnail.jpg")
                .build();
        ReflectionTestUtils.setField(product, "id", productId);

        given(addressRepository.findByIdAndUserId(addressId, userId)).willReturn(Optional.of(address));
        given(productRepository.findAllByIdWithLock(any())).willReturn(List.of(product));

        //when&then
        assertThatThrownBy(()->orderService.createOrder(userId,request))
                .isInstanceOf(CustomException.class)
                .satisfies(exception->{
                    CustomException ex = (CustomException) exception;
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getMessage()).isEqualTo("재고가 부족합니다.");
                });
    }

    @Test
    @DisplayName("주문 취소 성공 - 배송 완료 전 주문을 취소하면 상품 재고가 복구되고 상태가 CANCELED로 변경")
    void cancelOrder_success(){
        //given
        Long userId = 1L;
        Long orderId = 1L;
        Order order = Order.builder()
                .userId(userId)
                .totalPrice(18000)
                .orderStatus(OrderStatus.DELIVERING)
                .paymentMethod(PaymentMethod.CARD)
                .receiver("홍길동")
                .phone("01012345678")
                .address("테스트시 테스트로 123")
                .detailAddress("101호")
                .build();
        ReflectionTestUtils.setField(order, "id", orderId);

        Product product = Product.builder()
                .name("테스트 상품")
                .stock(8)
                .build();

        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .price(10000)
                .discountPrice(9000)
                .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(orderDetailRepository.findByOrderId(orderId)).willReturn(List.of(orderDetail));


        //when
        orderService.cancelOrder(userId, orderId);

        //then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(product.getStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("주문 조회 성공 - 내 주문 목록을 조회하면 페이징 및 주문 정보가 정상적으로 반환")
    void getMyOrders_success(){
        Long userId = 1L;
        int page = 0;
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Order order = Order.builder()
                .userId(userId)
                .totalPrice(18000)
                .orderStatus(OrderStatus.DELIVERING)
                .paymentMethod(PaymentMethod.CARD)
                .receiver("홍길동")
                .phone("01012345678")
                .address("테스트시 테스트로 123")
                .detailAddress("101호")
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);

        Slice<Order> orderSlice = new SliceImpl<>(List.of(order), pageable, false);

        Product product = Product.builder()
                .name("테스트상품")
                .price(10000)
                .image1("thumb.jpg")
                .build();
        ReflectionTestUtils.setField(product, "id", 100L);

        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .product(product)
                .quantity(1)
                .price(10000)
                .discountPrice(10000)
                .build();

        Payment payment = Payment.builder()
                .order(order)
                .status(PaymentStatus.SUCCESS)
                .paymentMethod(PaymentMethod.CARD)
                .build();

        given(orderRepository.findByUserId(eq(userId), any(Pageable.class))).willReturn(orderSlice);
        given(orderDetailRepository.findByOrderIdInWithProduct(any())).willReturn(List.of(orderDetail));
        given(paymentRepository.findByOrderIdIn(any())).willReturn(List.of(payment));

        // when
        OrderListResponse response = orderService.getMyOrders(userId, page);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrders()).hasSize(1);
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getOrders().get(0).getProductName()).isEqualTo("테스트상품");
        assertThat(response.getOrders().get(0).getPaymentStatus()).isEqualTo("SUCCESS");

    }

    @Test
    @DisplayName("주문 상세 조회 성공 - 주문 상세를 조회하면 전화번호 마스킹 및 상세 정보가 정상 반환")
    void getOrderDetail_success() {
        // given
        Long userId = 1L;
        Long orderId = 1L;

        Order order = Order.builder()
                .userId(userId)
                .totalPrice(10000)
                .orderStatus(OrderStatus.PAID)
                .paymentMethod(PaymentMethod.CARD)
                .receiver("홍길동")
                .phone("01012345678")
                .address("테스트시 테스트로 123")
                .detailAddress("101호")
                .build();
        ReflectionTestUtils.setField(order, "id", orderId);

        Product product = Product.builder()
                .name("테스트상품")
                .price(10000)
                .image1("thumb.jpg")
                .build();
        ReflectionTestUtils.setField(product, "id", 100L);

        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .product(product)
                .quantity(1)
                .price(10000)
                .discountPrice(10000)
                .build();

        Payment payment = Payment.builder()
                .order(order)
                .status(PaymentStatus.SUCCESS)
                .paymentMethod(PaymentMethod.CARD)
                .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));
        given(orderDetailRepository.findByOrderId(orderId)).willReturn(List.of(orderDetail));

        // when
        OrderDetailResponse response = orderService.getOrderDetail(userId, orderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getReceiver().getPhone()).isEqualTo("010-****-5678"); // 마스킹 검증
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getProductName()).isEqualTo("테스트상품");
    }

    @Test
    @DisplayName("배송 조회 성공 - 배송 중인 주문의 배송 정보를 조회하면 운송장 번호와 택배사 정보가 반환")
    void getDelivery_success() {
        // given
        Long userId = 1L;
        Long orderId = 1L;

        Order order = Order.builder()
                .userId(userId)
                .totalPrice(10000)
                .orderStatus(OrderStatus.DELIVERING) // 배송 중 상태
                .receiver("홍길동")
                .phone("01012345678")
                .address("테스트시 테스트로 123")
                .build();
        ReflectionTestUtils.setField(order, "id", orderId);
        setCreatedAt(order,LocalDateTime.now());

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // when
        DeliveryResponse response = orderService.getDelivery(userId, orderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getStatus()).isEqualTo("DELIVERING");
        assertThat(response.getTrackingNumber()).isEqualTo("1234567890");
        assertThat(response.getDeliveryCompany()).isEqualTo("CJ대한통운");
    }

    private void setCreatedAt(Object entity, LocalDateTime dateTime) {
        try {
            // 상속받은 부모 클래스(BaseTimeEntity)에 있는 createdAt 필드를 찾음
            Field field = entity.getClass().getSuperclass().getDeclaredField("createdAt");
            field.setAccessible(true); // private 필드 접근 허용
            field.set(entity, dateTime); // 값 강제 주입
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

