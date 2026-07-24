package com.apppang.apppang2.domain.product.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class ProductListResponse {
    private List<ProductResponse> products;
    private int page;
    private boolean hasNext;

    public ProductListResponse(List<ProductResponse> products, int page, boolean hasNext){
        this.products = products;
        this.page = page;
        this.hasNext = hasNext;
    }
}