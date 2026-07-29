package com.apppang.apppang2.domain.wishlist.entity;

import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.user.entity.User;
import com.apppang.apppang2.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "wishlists",
        //유니크 제약 추가. 사용자는 상품당 1개의 위시리스트만 생성가능
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wishlist extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishList_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder
    public Wishlist(User user, Product product){
        this.user = user;
        this.product = product;
    }
}
