package com.apppang.apppang2.domain.payment.entity;

import com.apppang.apppang2.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private int amount;                 //실제 결제된 금액

    //enum을 DB에 문자열로 저장
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;              //결제 상태

    private LocalDateTime paidAt;       //결제 완료 시간

    @Builder
    public Payment(Order order, PaymentMethod paymentMethod, int amount, PaymentStatus status){
        this.order = order;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = status;
        this.paidAt = LocalDateTime.now();
    }
}
