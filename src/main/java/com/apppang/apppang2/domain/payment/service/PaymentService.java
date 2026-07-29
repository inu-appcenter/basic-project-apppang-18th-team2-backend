package com.apppang.apppang2.domain.payment.service;

import com.apppang.apppang2.domain.cart.repository.CartRepository;
import com.apppang.apppang2.domain.order.entity.Order;
import com.apppang.apppang2.domain.order.entity.OrderDetail;
import com.apppang.apppang2.domain.order.entity.OrderStatus;
import com.apppang.apppang2.domain.order.repository.OrderDetailRepository;
import com.apppang.apppang2.domain.order.repository.OrderRepository;
import com.apppang.apppang2.domain.payment.dto.PaymentRequest;
import com.apppang.apppang2.domain.payment.dto.PaymentResponse;
import com.apppang.apppang2.domain.payment.entity.Payment;
import com.apppang.apppang2.domain.payment.entity.PaymentStatus;
import com.apppang.apppang2.domain.payment.repository.PaymentRepository;
import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import com.apppang.apppang2.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

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

        //재고 차감(OrderDetailRepository를 통해 상세 내역을 가져온다)
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());

        //상품 ID 목록을 추출
        List<Long> productIds = orderDetails.stream()
                .map(d -> d.getProduct().getId())
                .distinct() //중복 제거. 혹시 몰라서 추가. 지금 짜여진 로직대로면 필요없음
                .toList();

        //List로 하면 결과값과 현재 리스트를 처음부터 끝까지 비교하므로 O(n^2)이 되기에 Map 사용
        //쿼리 1회 호출
        Map<Long, Product> productMap = productRepository.findAllByIdWithLock(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for(OrderDetail detail: orderDetails){
            //product가 null일 가능성은 낮다고 느껴서 null 체크를 생략 (후에 필요하다면 추가)
            Product product = productMap.get(detail.getProduct().getId());
            product.decreaseStock(detail.getQuantity());
        }

        //주문 상태 및 결제 수단 업데이트
        order.updatePaymentInfo(OrderStatus.PAID, request.getPaymentMethod());

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(request.getPaymentMethod())
                .amount(order.getTotalPrice())
                .status(PaymentStatus.SUCCESS)                    //성공 보장, enum를 사용하도록 수정
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
                .paymentStatus(payment.getStatus().name()) //이름으로 받아와 String 타입을 맞춤
                .paidAt(payment.getPaidAt())
                .build();
    }

}
