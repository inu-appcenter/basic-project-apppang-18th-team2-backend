package com.apppang.apppang2.domain.payment.service;

import com.apppang.apppang2.domain.cart.repository.CartRepository;
import com.apppang.apppang2.domain.order.entity.Order;
import com.apppang.apppang2.domain.order.entity.OrderStatus;
import com.apppang.apppang2.domain.order.repository.OrderRepository;
import com.apppang.apppang2.domain.payment.dto.PaymentRequest;
import com.apppang.apppang2.domain.payment.dto.PaymentResponse;
import com.apppang.apppang2.domain.payment.entity.Payment;
import com.apppang.apppang2.domain.payment.entity.PaymentStatus;
import com.apppang.apppang2.domain.payment.repository.PaymentRepository;
import com.apppang.apppang2.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;

    @Transactional
    public PaymentResponse processPayment(Long userId, PaymentRequest request){
        //주문 조회
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND,"주문 정보를 찾을 수 없습니다."));

        if(!order.getUserId().equals(userId)){
            throw new CustomException(HttpStatus.FORBIDDEN,"본인의 주문만 결제할 수 있습니다.");
        }

        //사용자의 중복 결제 방지
        if(OrderStatus.PAID.equals(order.getOrderStatus())){
            throw new CustomException(HttpStatus.BAD_REQUEST,"이미 결제가 완료된 주문입니다.");
        }

        //재고는 주문 생성 시점(OrderService.createOrder)에 이미 차감되었으므로
        //결제 단계에서는 별도로 재고를 다루지 않음

        //주문 상태 및 결제 수단 업데이트
        order.updatePaymentInfo(OrderStatus.DELIVERING, request.getPaymentMethod());

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(request.getPaymentMethod())
                .amount(order.getTotalPrice())
                .status(PaymentStatus.SUCCESS)
                .build();

        paymentRepository.save(payment);

        //장바구니 결제라면 장바구니 비우기
        if(request.getIsFromCart()){
            cartRepository.deleteByUserId(userId);
        }

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(order.getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .paymentStatus(payment.getStatus().name())
                .paidAt(payment.getPaidAt())
                .build();
    }
}