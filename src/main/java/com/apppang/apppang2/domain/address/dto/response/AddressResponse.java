package com.apppang.apppang2.domain.address.dto.response;

import com.apppang.apppang2.domain.address.entity.Address;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AddressResponse {
    private Long addressId;
    private String receiver;
    private String phone;
    private String roadAddress;
    private String detailAddress;

    @JsonProperty("isDefault")  //이름 맞추기
    private boolean isDefault;

    @Builder
    public AddressResponse(Long addressId, String receiver, String phone, String roadAddress, String detailAddress, boolean isDefault){
        this.addressId = addressId;
        this.receiver = receiver;
        this.phone = phone;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.isDefault = isDefault;
    }

    public static AddressResponse from(Address address){
        return AddressResponse.builder()
                .addressId(address.getId())
                .receiver(address.getReceiver())
                .phone(address.getReceiverPhone())
                .roadAddress(address.getRoadAddress())
                .detailAddress(address.getDetailAddress())
                .isDefault(address.isDefault())
                .build();
    }
}
