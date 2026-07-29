package com.apppang.apppang2.domain.order.repository;

import com.apppang.apppang2.domain.order.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    //특정 주문번호의 모든 주문 상세 내역을 조회
    List<OrderDetail> findByOrderId(Long orderId);

    //orderId와 productId로 특정 주문 상세 내역을 찾는 메서드
    Optional<OrderDetail> findByOrderIdAndProductId(Long orderId, Long productId);

    //주문 ID 리스트로 상세 내역과 상품을 한 번에 페치 조인하여 조회
    @Query("SELECT od FROM OrderDetail od JOIN FETCH od.product WHERE od.order.id IN :orderIds")
    List<OrderDetail> findByOrderIdInWithProduct(@Param("orderIds") List<Long> orderIds);
}