package com.apppang.apppang2.domain.order.repository;

import com.apppang.apppang2.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}