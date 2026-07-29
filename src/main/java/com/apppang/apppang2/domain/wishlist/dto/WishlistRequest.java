package com.apppang.apppang2.domain.wishlist.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class WishlistRequest {
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;
}
