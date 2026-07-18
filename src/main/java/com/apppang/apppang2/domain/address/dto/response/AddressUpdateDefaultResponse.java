package com.apppang.apppang2.domain.address.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AddressUpdateDefaultResponse {
    private Long addressId;
    private boolean isDefault;

    public AddressUpdateDefaultResponse(Long addressId, boolean isDefault){
        this.addressId = addressId;
        this.isDefault = isDefault;
    }
}
