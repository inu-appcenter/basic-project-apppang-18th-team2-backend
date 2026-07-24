package com.apppang.apppang2.domain.cart.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class DeleteCartItemRequest {

    @NotEmpty(message = "삭제할 상품을 선택해주세요.")
    private List<Long> cartItemIds;
}