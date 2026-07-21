package com.apppang.apppang2.domain.review.entity;

import com.apppang.apppang2.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReviewLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reviewLike_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)   //한 물건 당 여러 리뷰 가능
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Builder
    public ReviewLike(Long id, User user, Review review){
        this.id = id;
        this.user = user;
        this.review = review;
    }
}
