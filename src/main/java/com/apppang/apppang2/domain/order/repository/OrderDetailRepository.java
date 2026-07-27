package com.apppang.apppang2.domain.order.repository;

import com.apppang.apppang2.domain.order.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    //특정 주문번호의 모든 주문 상세 내역을 조회
    List<OrderDetail> findByOrderId(Long orderId);

    //orderId와 productId로 특정 주문 상세 내역을 찾는 메서드
    Optional<OrderDetail> findByOrderIdAndProductId(Long orderId, Long productId);
}