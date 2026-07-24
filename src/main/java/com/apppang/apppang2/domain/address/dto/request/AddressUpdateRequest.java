package com.apppang.apppang2.domain.address.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddressUpdateRequest {
    private String receiver;
    private String receiverPhone;
    private String roadAddress;
    private String detailAddress;
}
