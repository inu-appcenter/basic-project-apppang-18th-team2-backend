package com.apppang.apppang2.domain.review.repository;

import com.apppang.apppang2.domain.review.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    //특정 유저의 해당 리뷰 '도움돼요' 클릭 여부 단건 확인
    Optional<ReviewLike> findByReviewIdAndUserId(Long reviewId, Long userId);

    //리뷰 목록 조회할 때 여러건 확인
    List<ReviewLike> findByUserIdAndReviewIdIn(Long userId, List<Long> reviewId);

    //특정 리뷰ID에 달린 도움돼요 데이터 모두 지움
    void deleteByReviewId(Long reviewId);
}
