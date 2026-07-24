package com.apppang.apppang2.domain.order.entity;

import com.apppang.apppang2.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int totalPrice;     //주문 시점의 총 결제 금액 (스냅샷)

    //enum을 DB에 "PAID" 같은 문자열로 저장
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private String paymentMethod;   //결제 연동 전이라 생성 시 "CARD" 고정

    //주문 후 배송지를 수정/삭제해도 이 주문의 배송 정보는 보존 (스냅샷)
    @Column(nullable = false)
    private String receiver;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    private String detailAddress;   //상세주소는 없을 수 있음

    @Builder
    public Order(Long userId, int totalPrice, OrderStatus orderStatus, String paymentMethod,
                 String receiver, String phone, String address, String detailAddress){
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.paymentMethod = paymentMethod;
        this.receiver = receiver;
        this.phone = phone;
        this.address = address;
        this.detailAddress = detailAddress;
    }
}