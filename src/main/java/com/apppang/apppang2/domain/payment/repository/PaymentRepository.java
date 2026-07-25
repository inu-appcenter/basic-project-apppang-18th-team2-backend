package com.apppang.apppang2.domain.payment.repository;

import com.apppang.apppang2.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
