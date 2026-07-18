package com.apppang.apppang2.domain.address.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AddressRequest {
    private String receiver;
    private String receiverPhone;
    private String roadAddress;
    private String detailAddress;
    private boolean isDefault;

    @Builder
    public updateAddressRequest{
        this.receiver = receiver;
        this.receiverPhone = receiverPhone;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
    }
}
