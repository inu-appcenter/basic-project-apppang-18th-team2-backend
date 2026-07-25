package com.apppang.apppang2.domain.payment.dto;

import com.apppang.apppang2.domain.payment.entity.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRequest {
    @NotNull(message = "주문번호는 필수입니다.")
    private Long orderId;

    @NotNull(message = "결제수단은 필수입니다.")
    private PaymentMethod paymentMethod;

    @JsonProperty("isFromCart")
    //boolean 사용 시 Swagger에 필드가 중복 노출되어 Boolean으로 변경
    private Boolean isFromCart = false;    //null 방어를 위해 기본값 false로 설정. 바로결제면 false, 장바구니결제면 true
}
