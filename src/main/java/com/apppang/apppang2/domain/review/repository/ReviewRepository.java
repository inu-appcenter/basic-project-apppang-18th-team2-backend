package com.apppang.apppang2.domain.review.repository;

import com.apppang.apppang2.domain.review.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ReviewRepository extends JpaRepository<Review, Long> {

    //orderDetailId로 이미 작성된 리뷰가 있는지 확인
    boolean existsByOrderDetailId(Long orderDetailId);

    //주문상세에 작성된 리뷰 조회 (주문 상세 응답의 리뷰 정보용)
    Optional<Review> findByOrderDetailId(Long orderDetailId);

    //상품Id로 리뷰를 조회하며 Pageable 규격에 따라 페이징 및 정렬 처리
    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.productId = :productId")
    Slice<Review> findReviewsWithUserByProductId(@Param("productId") Long productId, Pageable pageable);

    @Query("SELECT r.imageUrl1 FROM Review r WHERE r.imageUrl1 IS NOT NULL")
    List<String> findAllImageUrl1();

    @Query("SELECT r.imageUrl2 FROM Review r WHERE r.imageUrl2 IS NOT NULL")
    List<String> findAllImageUrl2();

    //상품의 리뷰 평점 평균 (리뷰가 없으면 null)
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Double averageRatingByProductId(@Param("productId") Long productId);

    //상품의 리뷰 수 (파생 쿼리)
    long countByProductId(Long productId);
}
