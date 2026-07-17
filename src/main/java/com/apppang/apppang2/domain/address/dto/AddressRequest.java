package com.apppang.apppang2.domain.address.dto;

import lombok.Getter;

@Getter
public class AddressRequest {
    private String receiver;
    private String receiverPhone;
    private String roadAddress;
    private String detailAddress;
    private boolean isDefault;
}
