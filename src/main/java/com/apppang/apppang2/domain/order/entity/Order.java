package com.apppang.apppang2.domain.order.entity;

import com.apppang.apppang2.domain.payment.entity.PaymentMethod;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    //주문 후 배송지를 수정/삭제해도 이 주문의 배송 정보는 보존 (스냅샷)
    @Column(nullable = false)
    private String receiver;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    private String detailAddress;   //상세주소는 없을 수 있음

    @Builder
    public Order(Long userId, int totalPrice, OrderStatus orderStatus, PaymentMethod paymentMethod,
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

    //결제 완료 후 주문의 상태와 결제 수단을 업데이트
    public void updatePaymentInfo(OrderStatus orderStatus, PaymentMethod paymentMethod){
        this.orderStatus = orderStatus;
        this.paymentMethod = paymentMethod;
    }

    //주문 취소가 발생했을 때 orderStatus만 단독으로 변경하는 메서드
    public void updateOrderStatus(OrderStatus orderStatus){
        this.orderStatus = orderStatus;
    }
}