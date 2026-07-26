package com.apppang.apppang2.domain.order.service;

import com.apppang.apppang2.domain.address.entity.Address;
import com.apppang.apppang2.domain.address.repository.AddressRepository;
import com.apppang.apppang2.domain.order.dto.request.CreateOrderRequest;
import com.apppang.apppang2.domain.order.dto.request.OrderItemRequest;
import com.apppang.apppang2.domain.order.dto.response.CreateOrderResponse;
import com.apppang.apppang2.domain.order.dto.response.OrderDetailResponse;
import com.apppang.apppang2.domain.order.dto.response.OrderListResponse;
import com.apppang.apppang2.domain.order.dto.response.OrderResponse;
import com.apppang.apppang2.domain.order.dto.response.DeliveryResponse;
import com.apppang.apppang2.domain.order.entity.*;
import com.apppang.apppang2.domain.order.repository.DeliveryRepository;
import com.apppang.apppang2.domain.order.repository.OrderDetailRepository;
import com.apppang.apppang2.domain.order.repository.OrderRepository;
import com.apppang.apppang2.domain.payment.entity.PaymentMethod;
import com.apppang.apppang2.domain.payment.repository.PaymentRepository;
import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import com.apppang.apppang2.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository; //결제 상태 조회도 하기 위해 추가
    private final DeliveryRepository deliveryRepository; //배송 레포지토리 연결

    //주문 생성: 상품 검증 → 재고 차감 → 주문 저장 → 주문 상세 저장이 전부 한 트랜잭션
    //중간에 예외가 나면 원상복구됨
    @Transactional
    public CreateOrderResponse createOrder(Long userId, CreateOrderRequest request){

        //1. 배송지 조회 — 없거나 남의 배송지면 404
        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "배송지를 찾을 수 없습니다."));

        //2. 상품 검증 + 재고 차감 + 총액 계산 (상세 저장에 쓸 상품들을 순서대로 보관)
        List<Product> products = new ArrayList<>();
        int totalPrice = 0;

        for (OrderItemRequest item : request.getItems()){
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

            if (product.getStock() < item.getQuantity()){
                throw new CustomException(HttpStatus.CONFLICT, "재고가 부족합니다.");
            }

            product.decreaseStock(item.getQuantity());      //재고 감소
            totalPrice += product.getSalePrice() * item.getQuantity();
            products.add(product);
        }

        //3. 주문 저장 (총액 계산이 끝난 뒤에야 저장 가능)
        Order order = orderRepository.save(Order.builder()
                .userId(userId)
                .totalPrice(totalPrice)
                .orderStatus(OrderStatus.PENDING)      //결제가 안 끝났으므로 결제 대기 상태로 저장
                .paymentMethod(PaymentMethod.NONE)     //결제 방식 미정
                .receiver(address.getReceiver())
                .phone(address.getReceiverPhone())
                .address(address.getRoadAddress())
                .detailAddress(address.getDetailAddress())
                .build());

        //4. 주문 상세 저장 (주문 당시 가격을 스냅샷으로 복사)
        List<OrderDetail> details = new ArrayList<>();
        for (int i = 0; i < request.getItems().size(); i++){
            Product product = products.get(i);
            details.add(OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(request.getItems().get(i).getQuantity())
                    .price(product.getPrice())
                    .discountPrice(product.getSalePrice())
                    .build());
        }
        orderDetailRepository.saveAll(details);

        return new CreateOrderResponse(order.getId(), totalPrice);
    }
    //주문 목록 조회 로직
    public OrderListResponse getMyOrders(Long userId, int page){
        //생성일 역순으로 정렬해서 10개씩 가져옴
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);

        List<OrderResponse> orders = orderPage.getContent().stream()
                .map(this::toOrderResponse)
                .toList();

        return new OrderListResponse(orders, page, orderPage.hasNext());
    }

    //주문 엔티티 하나를 응답 DTO로 변환하는 메소드
    private OrderResponse toOrderResponse (Order order){
        //주문에 담긴 상품 상세 목록
        //대표 상품은 첫번째 상품으로 설정함
        List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getId());
        Product representativeProduct = details.get(0).getProduct();

        //결제 전이라면 Payment가 없으므로 null
        String paymentStatus = paymentRepository.findByOrderId(order.getId())
                .map(payment -> payment.getStatus().name())
                .orElse(null);

        return OrderResponse.builder()
                .orderId(order.getId())
                .orderedAt(order.getCreatedAt())
                .orderStatus(order.getOrderStatus().name())   //기존 OrderStatus enum 값 그대로 사용
                .paymentStatus(paymentStatus)
                .totalPrice(order.getTotalPrice())
                .thumbnail(representativeProduct.getImage1())
                .productName(representativeProduct.getName())
                .itemCount(details.size())
                .build();
    }

    //주문 상세 조회 로직. 본인 주문이 아니거나 없으면 404 반환
    public OrderDetailResponse getOrderDetail(Long userId, Long orderId){
        Order order = orderRepository.findById(orderId)
                .filter(o->o.getUserId().equals(userId))//주문 ID를 꺼내온 후 본인 주문이 아니라면 조회 실패
                .orElseThrow(()-> new CustomException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        //아직 결제 정보가 없다면 전부 null로 처리
        OrderDetailResponse.PaymentInfo paymentInfo = paymentRepository.findByOrderId(orderId)
                .map(p -> OrderDetailResponse.PaymentInfo.builder()
                        .paymentMethod(p.getPaymentMethod().name())
                        .paymentStatus(p.getStatus().name())
                        .paidAt(p.getPaidAt())
                        .build())
                .orElse(OrderDetailResponse.PaymentInfo.builder().build());

        //수령인 전화번호 마스킹 처리
        OrderDetailResponse.ReceiverInfo receiverInfo = OrderDetailResponse.ReceiverInfo.builder()
                .name(order.getReceiver())
                .phone(maskPhone(order.getPhone()))
                .build();

        //배송지 정보
        OrderDetailResponse.AddressInfo addressInfo = OrderDetailResponse.AddressInfo.builder()
                .roadAddress(order.getAddress())
                .detailAddress(order.getDetailAddress())
                .build();

        //상품별 상세
        //할인율은 스냅샷 가격 기준으로 할인율을 역산
        List<OrderDetail> details = orderDetailRepository.findByOrderId(orderId);
        List<OrderDetailResponse.OrderItemInfo> items = details.stream()
                .map(this::toOrderItemInfo)
                .toList();

        int productPrice = details.stream().mapToInt(d-> d.getPrice()*d.getQuantity()).sum();
        int discountPrice = productPrice - order.getTotalPrice();

        //결제 요약
        OrderDetailResponse.SummaryInfo summaryInfo = OrderDetailResponse.SummaryInfo.builder()
                .productPrice(productPrice)
                .deliveryFee(0)
                .discountPrice(discountPrice)
                .totalPrice(order.getTotalPrice())
                .build();

        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .orderedAt(order.getCreatedAt())
                .orderStatus(order.getOrderStatus().name())
                .payment(paymentInfo)
                .receiver(receiverInfo)
                .address(addressInfo)
                .items(items)
                .summary(summaryInfo)
                .build();
    }

    //마스킹 메서드
    //명세서 조건에 맞춰 01012345678이라면 010-****-5678로 반환
    private String maskPhone(String phone){
        //전화번호 조건에 안 맞는 정보가 들어온다면 그대로 반환
        if(phone==null || !phone.matches("\\d{11}")) return phone;
        return phone.substring(0,3) + "-****-" + phone.substring(7);
    }

    //OrderDetail을 응답 상품 정보로 변환하는 메서드
    private OrderDetailResponse.OrderItemInfo toOrderItemInfo(OrderDetail detail){
        int originalPrice = detail.getPrice();
        int salePrice = detail.getDiscountPrice();
        int discountRate = originalPrice == 0 ? 0 : (originalPrice - salePrice) * 100 / originalPrice;

        return OrderDetailResponse.OrderItemInfo.builder()
                .productId(detail.getProduct().getId())
                .productName(detail.getProduct().getName())
                .thumbnail(detail.getProduct().getImage1())
                .originalPrice(originalPrice)
                .discountRate(discountRate)
                .salePrice(salePrice)
                .quantity(detail.getQuantity())
                .totalPrice(salePrice * detail.getQuantity())
                .build();
    }

    //주문 취소 로직. 배송 시작 전(PENDING/PAID/PREPARING)까지만 취소 가능
    @Transactional //DB값을 수정해야하므로
    public void cancelOrder(Long userId, Long orderId){
        //주문을 조회하여 존재하지 않거나 userId와 일치하지 않는다면 404
        Order order =orderRepository.findById(orderId)
                .filter(o->o.getUserId().equals(userId))
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        //배송 시작 이후인지 확인. 만약 그렇다면 취소불가
        if (!isCancelable(order.getOrderStatus())){
            throw new CustomException(HttpStatus.CONFLICT, "배송이 시작된 주문은 취소할 수 없습니다.");
        }

        //주문에 담긴 상품들의 재고를 복구
        List<OrderDetail> details = orderDetailRepository.findByOrderId(orderId);
        for (OrderDetail detail : details){
            detail.getProduct().increaseStock(detail.getQuantity());
        }

        //주문을 취소해도 내역은 남아야하므로 상태만 CANCELED로 변경
        order.updateOrderStatus(OrderStatus.CANCELED);
    }

    //취소 가능한 상태를 PENDING/PAID/PREPARING으로 지정하는 메서드
    private boolean isCancelable(OrderStatus status){
        return status == OrderStatus.PENDING
                || status == OrderStatus.PAID
                || status == OrderStatus.PREPARING;
    }

    /*
    배송 조회 로직
    현재는 배송 도메인이 따로 없어 직접 DB를 건드리는게 아니면 배송 정보 지정이 불가능
    임시로 일정시간이 지나면 배송 정보가 생성되게 구현
     */
    @Transactional  //DB값을 수정해야하므로 GET이지만 추가
    public DeliveryResponse getDelivery (Long userId,Long orderId){

        //주문이 없거나 사용자가 동일하지 않으면 404
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getUserId().equals(userId))
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "배송 정보를 찾을 수 없습니다."));

        //조회 시점에 배송 시작 조건을 검증
        //조건 충족 시 배송 정보 생성
        checkAndCreateDelivery(order);

        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "배송 정보를 찾을 수 없습니다."));

        return DeliveryResponse.builder()
                .orderId(order.getId())
                .status(delivery.getDeliveryStatus().name())
                .trackingNumber(delivery.getTrackingNumber())
                .deliveryCompany(delivery.getCourier())
                .estimatedArrival(delivery.getCompletedAt().toLocalDate())
                .build();
    }

    //주문 조회 진입시 호출
    //배송 시작 조건(주문 후 10분)을 검증하고 필요하다면 배송 정보를 생성함
    private void checkAndCreateDelivery(Order order){

        if (deliveryRepository.findByOrderId(order.getId()).isPresent()){
            return;
        }

        //이미 배송 정보가 있다면 배송 정보를 생성하지 않음
        LocalDateTime deliveryStartTime = order.getCreatedAt().plusMinutes(10);
        if (LocalDateTime.now().isBefore(deliveryStartTime)){
            return;
        }

        //10분 경과 + 아직 배송정보 없음 → 배송정보 생성 + Order 상태 동기화
        Delivery delivery = Delivery.builder()
                .order(order)
                .trackingNumber("1234567890")
                .deliveryStatus(DeliveryStatus.DELIVERING)
                .courier("CJ대한통운")
                .startedAt(deliveryStartTime)
                .completedAt(deliveryStartTime.plusDays(3))
                .build();
        deliveryRepository.save(delivery);

        order.updateOrderStatus(OrderStatus.DELIVERING);
    }
}