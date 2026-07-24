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

    @JsonProperty("isDefault")  //이름 맞추기
    private boolean isDefault;
}
