package com.apppang.apppang2.domain.order.repository;

import com.apppang.apppang2.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    //전체 주문 조회를 위해 추가
    //Page<Order> findByUserId(Long userId, Pageable pageable);
    Slice<Order> findByUserId(Long userId, Pageable pageable);
}