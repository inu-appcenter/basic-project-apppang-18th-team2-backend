package com.apppang.apppang2.domain.address.dto.response;

import com.apppang.apppang2.domain.address.entity.Address;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressResponse {
    private Long addressId;
    private String receiver;
    private String phone;
    private String roadAddress;
    private String detailAddress;
    private boolean isDefault;


    @Builder
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
