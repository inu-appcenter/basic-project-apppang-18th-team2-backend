package com.apppang.apppang2.domain.address.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressUpdateRequest {
    @NotBlank(message = "수령인을 입력해주세요.")
    private String receiver;
    @NotBlank(message = "수령인 연락처를 입력해주세요")
    private String receiverPhone;
    @NotBlank(message = "주소를 입력해주세요")
    private String roadAddress;
    private String detailAddress;
}
