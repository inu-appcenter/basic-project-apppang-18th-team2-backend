package com.apppang.apppang2.domain.order.entity;

import com.apppang.apppang2.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//주문에 담긴 상품 1개
@Entity
@Table(name = "order_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int price;          //주문 당시 정가 (스냅샷)

    @Column(nullable = false)
    private int discountPrice;  //주문 당시 판매가 (할인 없으면 정가와 동일)

    @Builder
    public OrderDetail(Order order, Product product, int quantity, int price, int discountPrice){
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.discountPrice = discountPrice;
    }
}