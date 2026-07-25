package com.apppang.apppang2.domain.order.service;

import com.apppang.apppang2.domain.address.entity.Address;
import com.apppang.apppang2.domain.address.repository.AddressRepository;
import com.apppang.apppang2.domain.order.dto.request.CreateOrderRequest;
import com.apppang.apppang2.domain.order.dto.request.OrderItemRequest;
import com.apppang.apppang2.domain.order.dto.response.CreateOrderResponse;
import com.apppang.apppang2.domain.order.entity.Order;
import com.apppang.apppang2.domain.order.entity.OrderDetail;
import com.apppang.apppang2.domain.order.entity.OrderStatus;
import com.apppang.apppang2.domain.order.repository.OrderDetailRepository;
import com.apppang.apppang2.domain.order.repository.OrderRepository;
import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import com.apppang.apppang2.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;

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
                .orderStatus(OrderStatus.PAID)      //결제 연동 전: 생성 즉시 결제 완료로 간주
                .paymentMethod("CARD")              //결제 방식 일단 카드결제로
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
}