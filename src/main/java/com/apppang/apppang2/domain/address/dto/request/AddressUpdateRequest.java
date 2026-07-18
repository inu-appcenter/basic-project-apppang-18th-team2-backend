package com.apppang.apppang2.domain.address.dto.request;

import lombok.Getter;

@Getter
public class AddressUpdateRequest {
    private String receiver;
    private String receiverPhone;
    private String roadAddress;
    private String detailAddress;
}
