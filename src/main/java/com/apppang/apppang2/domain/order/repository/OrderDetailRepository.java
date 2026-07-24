package com.apppang.apppang2.domain.order.repository;

import com.apppang.apppang2.domain.order.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}