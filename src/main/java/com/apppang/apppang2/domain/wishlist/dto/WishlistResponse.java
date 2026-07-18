package com.apppang.apppang2.domain.wishlist.dto;

import com.apppang.apppang2.domain.product.dto.response.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WishlistResponse {
    private List<ProductResponse> products;
}
