package com.apppang.apppang2.domain.product.entity;

import com.apppang.apppang2.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    // ponytail: 카테고리 도메인이 아직 없어서 FK 값만 보관
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private String image1;      //대표 이미지

    @Column(nullable = false)
    private String image2;      //상세 이미지

    private Integer discountRate;   //할인율(%), 할인 없으면 null

    private Integer discountPrice;  //할인가, 할인 없으면 null

    //실제 판매가: 할인가가 있으면 할인가, 없으면 정가
    //DB에 생기는 게 아니라 조회 시 SQL의 coalesce로 계산되는 읽기 전용 값
    @Formula("coalesce(discount_price, price)")
    private int salePrice;

    @Column(nullable = false)
    private double ratingAvg;

    @Column(nullable = false)
    private int ratingCount;

    //상품이 참여 중인 이벤트 이름 목록 (여러 이벤트 동시 참여 가능)
    @ElementCollection
    @CollectionTable(name = "product_event", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "event")
    private Set<String> events = new HashSet<>();

    @Builder
    public Product(Long categoryId, String name, String description, int price, int stock,
                   String image1, String image2, Integer discountRate, Integer discountPrice){
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.image1 = image1;
        this.image2 = image2;
        this.discountRate = discountRate;
        this.discountPrice = discountPrice;
        this.ratingAvg = 0;     //리뷰가 생기면 갱신
        this.ratingCount = 0;
    }
}