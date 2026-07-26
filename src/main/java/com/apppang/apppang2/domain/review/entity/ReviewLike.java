package com.apppang.apppang2.domain.review.entity;

import com.apppang.apppang2.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "review_likes", uniqueConstraints = {
        //유니크 제약조건 추가
        @UniqueConstraint(name = "uk_review_like_user_review",
                columnNames = {"user_id", "review_id"}
        )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reviewLike_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Builder
    public ReviewLike(Long userId, Long reviewId){
        this.userId = userId;
        this.reviewId = reviewId;
    }
}
