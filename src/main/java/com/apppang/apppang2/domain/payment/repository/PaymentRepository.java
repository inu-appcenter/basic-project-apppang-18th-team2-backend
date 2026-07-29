package com.apppang.apppang2.domain.payment.repository;

import com.apppang.apppang2.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    //주문 조회 API에서 결제 상황도 받아야 하기에 추가함
    Optional<Payment> findByOrderId(Long orderId);

    //주문ID 리스트로 결제 정보들을 한번에 조회
    List<Payment> findByOrderIdIn(List<Long> orderIds);

}
