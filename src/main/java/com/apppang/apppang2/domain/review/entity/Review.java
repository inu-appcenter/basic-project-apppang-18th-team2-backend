package com.apppang.apppang2.domain.review.entity;

import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.user.entity.User;
import com.apppang.apppang2.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Table(name = "reviews")
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)   //한 물건 당 여러 리뷰 가능
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /* 주문상세가 구현되면 완성
    @OneToOne(fetch = FetchType.LAZY)   //한 주문상세 당 하나의 리뷰만 작성 가능
    @JoinColumn(name = "order_detail_id", nullable = false)
    private OrderDetail orderDetail;
    */

    @Column(nullable = false)
    private double rating;

    @Column(columnDefinition = "TEXT")  //255자 제한 걸지말고 수만글자 들어갈 수 있는 텍스트타입
    private String content;     //리뷰 내용

    @Column(name = "help_count", nullable = false)
    private int helpCount;

    @Column(name = "image_url_1")
    private String imageUrl1;

    @Column(name = "image_url_2")
    private String imageUrl2;

    @Builder
    public Review(User user, Product product, double rating, String content, String imageUrl1, String imageUrl2){
        this.user = user;
        this.product = product;
        //this.orderDetail = orderDetail;
        this.rating = rating;
        this.content = content;
        this.imageUrl1 = imageUrl1;
        this.imageUrl2 = imageUrl2;
    }

    //리뷰 수정
    public void update(double rating, String content){
        this.rating = rating;
        this.content = content;
    }

    //도움돼요 계산
    public void increaseHelpCount(){
        this.helpCount++;
    }

    public void decreaseHelpCount(){
        if(this.helpCount > 0){
            this.helpCount--;
        }
    }
}
