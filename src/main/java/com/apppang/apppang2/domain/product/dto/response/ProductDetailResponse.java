package com.apppang.apppang2.domain.product.dto.response;

import com.apppang.apppang2.domain.product.entity.Product;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductDetailResponse {
    private Long productId;
    private String name;
    private int originalPrice;
    private int discountRate;
    private int salePrice;
    private int stock;
    private double rating;
    private int reviewCount;
    private boolean wish;
    private String description;
    private List<String> images;

    public ProductDetailResponse(Product product){
        this.productId = product.getId();
        this.name = product.getName();
        this.originalPrice = product.getPrice();
        this.discountRate = product.getDiscountRateOrZero(); //반복된 코드를 Product가 소유하는 메서드로 전환
        this.salePrice = product.getSalePrice();
        this.stock = product.getStock();
        this.rating = product.getRatingAvg();
        this.reviewCount = product.getRatingCount();
        this.wish = false;  //찜 도메인 구현 후 로그인 사용자 기준으로 채우기
        this.description = product.getDescription();
        //대표 이미지 + 상세 이미지
        this.images = List.of(product.getImage1(), product.getImage2());
    }
}