package com.apppang.apppang2.domain.address.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressRequest {
    private String receiver;
    private String receiverPhone;
    private String roadAddress;
    private String detailAddress;

    @JsonProperty("isDefault")  //JSON으로 나갈때는 기존처럼 isDefault로 매핑
    private boolean defaultAddress;     //내부 필드명은 defaultAddress로
}
