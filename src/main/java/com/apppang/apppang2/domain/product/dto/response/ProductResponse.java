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

    //찜 여부를 직접 받을 수 있는 새로운 생성자 추가
    public ProductResponse(Product product, boolean isWish){
        this.productId = product.getId();
        this.name = product.getName();
        this.thumbnail = product.getImage1();
        this.originalPrice = product.getPrice();
        this.discountRate = product.getDiscountRate() == null ? 0 : product.getDiscountRate();
        this.salePrice = product.getSalePrice();
        this.rating = product.getRatingAvg();
        this.reviewCount = product.getRatingCount();
        this.wish = isWish;  //찜 도메인 구현 후 로그인 사용자 기준으로 채우기
    }

    //일반 상품 목록 조회할 때
    public ProductResponse(Product product){
        this(product, false);   //찜 여부를 안 넘기면 기본값인 false로 세팅
    }
}