package com.apppang.apppang2.domain.cart.entity;

import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    //일대다 관계, FK 설정
    //LAZY로 조회 시 상품을 무조건 같이 가져오지 않고 상품을 조회할때 가져오도록 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Builder
    public Cart(Long userId, Product product, int quantity){
        this.userId = userId;
        this.product = product;
        this.quantity = quantity;
    }

    //이미 담긴 상품을 또 담을 때 수량만 증가
    public void addQuantity(int amount){
        this.quantity += amount;
    }
    //수량 조절
    public void updateQuantity(int quantity){
        this.quantity = quantity;
    }
}