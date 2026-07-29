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
import com.apppang.apppang2.domain.order.repository.OrderDetailRepository;
import com.apppang.apppang2.domain.order.repository.OrderRepository;
import com.apppang.apppang2.domain.payment.entity.Payment;
import com.apppang.apppang2.domain.payment.entity.PaymentMethod;
import com.apppang.apppang2.domain.payment.repository.PaymentRepository;
import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import com.apppang.apppang2.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository; //결제 상태 조회도 하기 위해 추가

    //주문 생성: 상품 검증 → 재고 차감 → 주문 저장 → 주문 상세 저장이 전부 한 트랜잭션
    //중간에 예외가 나면 원상복구됨
    @Transactional
    public CreateOrderResponse createOrder(Long userId, CreateOrderRequest request){

        //1. 배송지 조회 — 없거나 남의 배송지면 404
        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "배송지를 찾을 수 없습니다."));

        //주문 상품ID 목록 추출
        List<Long> productIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .sorted()       //상품ID 리스트를 오름차순으로 정렬하여 동일한 순서로 락을 획득
                .toList();

        //동시성 제어를 위해 비관적 락 적용하여 DB 쿼리 1번으로 필요한 상품을 일괄 조회
        List<Product> products = productRepository.findAllByIdWithLock(productIds);

        //상품 찾아오기(요청된 상품 개수와 실제 조회된 개수가 다르면 예외처리)
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p->p));

        int totalPrice = 0;

        //2. 상품 검증 + 재고 차감 + 총액 계산 (상세 저장에 쓸 상품들을 순서대로 보관)
        for (OrderItemRequest item : request.getItems()){
            Product product = productMap.get(item.getProductId());
            if(product==null){
                throw new CustomException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");
            }

            if (product.getStock() < item.getQuantity()){
                throw new CustomException(HttpStatus.CONFLICT, "재고가 부족합니다.");
            }

            product.decreaseStock(item.getQuantity());      //재고 감소
            totalPrice += product.getSalePrice() * item.getQuantity();  //총 결제 금액 누적 계산(할인가 기준)
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
        for (OrderItemRequest item : request.getItems()){
            Product product = productMap.get(item.getProductId());  //상품ID로 Map에서 꺼냄
            details.add(OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(product.getPrice())
                    .discountPrice(product.getSalePrice())
                    .build());
        }
        orderDetailRepository.saveAll(details);

        return new CreateOrderResponse(order.getId(), totalPrice);
    }

    //주문 목록 조회 로직
    //DB값을 수정할 수 있으므로 추가(배송)
    public OrderListResponse getMyOrders(Long userId, int page){
        //생성일 역순으로 정렬해서 10개씩 가져옴
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Slice<Order> orderSlice = orderRepository.findByUserId(userId, pageable);

        List<Order> orders = orderSlice.getContent();
        //조회된 주문이 없는 경우 빈 리스트 반환
        if(orders.isEmpty()){
            return new OrderListResponse(Collections.emptyList(), page, orderSlice.hasNext());
        }

        //현재 페이지에 노출할 주문들의 ID만 추출
        List<Long> orderIds = orders.stream()
                .map(Order::getId)
                .toList();

        //IN절을 사용하여 주문 상세와 결제 정보를 각각 1번의 쿼리로 조회
        Map<Long, List<OrderDetail>> orderDetailMap = orderDetailRepository.findByOrderIdInWithProduct(orderIds).stream()
                .collect(Collectors.groupingBy(detail->detail.getOrder().getId()));

        Map<Long, Payment> paymentMap = paymentRepository.findByOrderIdIn(orderIds).stream()
                .collect(Collectors.toMap(payment->payment.getOrder().getId(), payment -> payment));

        //기존 메서드를 활용하되 Map을 넘겨주어 추가 쿼리 없이 DTO 조합 -- 설명하고 지우기
        List<OrderResponse> orderResponses = orders.stream()
                .map(order -> toOrderResponse(order, orderDetailMap, paymentMap))
                .toList();

        return new OrderListResponse(orderResponses, page, orderSlice.hasNext());
    }

    //주문 엔티티 하나를 응답 DTO로 변환하는 메소드
    private OrderResponse toOrderResponse (Order order, Map<Long, List<OrderDetail>> orderDetailMap,
                                           Map<Long, Payment> paymentMap ){
        //Map에서 현재 주문ID에 해당하는 상세 상품 목록 추출
        List<OrderDetail> details = orderDetailMap.getOrDefault(order.getId(),Collections.emptyList());

        //대표 상품은 첫번째 상품으로 설정함
        Product representativeProduct = details.isEmpty() ? null : details.get(0).getProduct();

        //Map에서 현재 주문ID에 해당하는 결제 정보 추출
        Payment payment = paymentMap.get(order.getId());
        String paymentStatus = (payment!=null) ? payment.getStatus().name() : null;

        return OrderResponse.builder()
                .orderId(order.getId())
                .orderedAt(order.getCreatedAt())
                .orderStatus(order.getOrderStatus().name())   //기존 OrderStatus enum 값 그대로 사용
                .paymentStatus(paymentStatus)
                .totalPrice(order.getTotalPrice())
                .thumbnail(representativeProduct != null ? representativeProduct.getImage1():null)
                .productName(representativeProduct != null ? representativeProduct.getName():"상품정보없음")
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
        //이미 취소된 주문이라면 취소 불가
        if (order.getOrderStatus()==OrderStatus.CANCELED){
            throw new CustomException(HttpStatus.CONFLICT, "이미 취소된 주문은 취소할 수 없습니다.");
        }

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

    //취소 가능한 상태를 PENDING/PAID/PREPARING/DELIVERING으로 지정하는 메서드
    private boolean isCancelable(OrderStatus status){
        return status == OrderStatus.PENDING
                || status == OrderStatus.PAID
                || status == OrderStatus.PREPARING
                || status == OrderStatus.DELIVERING;
    }

    //   배송 조회 로직
    public DeliveryResponse getDelivery (Long userId,Long orderId){

        //주문이 없거나 사용자가 동일하지 않으면 404
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getUserId().equals(userId))
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "배송 정보를 찾을 수 없습니다."));

        OrderStatus status = order.getOrderStatus();
        //배송 시작 전이거나 취소된 주문이면 배송 정보가 없는걸로 처리
        if(status != OrderStatus.DELIVERING && status != OrderStatus.DELIVERED){
            throw new CustomException(HttpStatus.NOT_FOUND, "배송 정보를 찾을 수 없습니다.");
        }

        return DeliveryResponse.builder()
                .orderId(order.getId())
                .status(status.name())
                .trackingNumber("1234567890")   //고정값 반환
                .deliveryCompany("CJ대한통운")      //고정값 반환
                .estimatedArrival(order.getCreatedAt().plusDays(5).toLocalDate())   //주문일 + 5일로 계산
                .build();
    }

}