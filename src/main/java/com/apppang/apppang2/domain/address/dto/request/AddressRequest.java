package com.apppang.apppang2.domain.address.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressRequest {
    @NotBlank(message = "수령인을 입력해주세요.")
    private String receiver;
    @NotBlank(message = "수령인 연락처를 입력해주세요")
    private String receiverPhone;
    @NotBlank(message = "주소를 입력해주세요")
    private String roadAddress;
    private String detailAddress;

    @JsonProperty("isDefault")  //JSON으로 나갈때는 기존처럼 isDefault로 매핑
    private boolean defaultAddress;     //내부 필드명은 defaultAddress로
}
