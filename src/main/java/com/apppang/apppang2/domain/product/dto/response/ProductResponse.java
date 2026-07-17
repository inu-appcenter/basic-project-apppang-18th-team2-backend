package com.apppang.apppang2.domain.product.dto.response;

import com.apppang.apppang2.domain.product.entity.Product;
import lombok.Getter;

@Getter
public class ProductResponse {

    private Long productId;
    private String name;
    private String thumbnail;
    private int originalPrice;
    private int discountRate;
    private int salePrice;
    private double rating;
    private int reviewCount;
    private boolean wish;

    public ProductResponse(Product product){
        this.productId = product.getId();
        this.name = product.getName();
        this.thumbnail = product.getImage1();
        this.originalPrice = product.getPrice();
        this.discountRate = product.getDiscountRate() == null ? 0 : product.getDiscountRate();
        this.salePrice = product.getSalePrice();
        this.rating = product.getRatingAvg();
        this.reviewCount = product.getRatingCount();
        this.wish = false;  //찜 도메인 구현 후 로그인 사용자 기준으로 채우기
    }
}