package com.apppang.apppang2.domain.payment.controller;

import com.apppang.apppang2.domain.payment.dto.PaymentRequest;
import com.apppang.apppang2.domain.payment.dto.PaymentResponse;
import com.apppang.apppang2.domain.payment.service.PaymentService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PAYMENT")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 요청")
    @PostMapping("/api/payments")
    public ResponseEntity<ApiResponse<PaymentResponse>> payment(@AuthenticationPrincipal Long userId,
                                                                @Valid @RequestBody PaymentRequest request){
        PaymentResponse response = paymentService.processPayment(userId, request);

        return ResponseEntity.ok(ApiResponse.success("결제가 완료되었습니다.",response));
    }

}
