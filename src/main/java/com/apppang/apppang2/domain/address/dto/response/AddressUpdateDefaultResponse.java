package com.apppang.apppang2.domain.address.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AddressUpdateDefaultResponse {
    private Long addressId;

    @JsonProperty("isDefault")  //JSON으로 나갈때는 기존처럼 isDefault로 매핑
    private boolean defaultAddress;

    @Builder
    public AddressUpdateDefaultResponse(Long addressId, boolean defaultAddress){
        this.addressId = addressId;
        this.defaultAddress = defaultAddress;
    }
}
