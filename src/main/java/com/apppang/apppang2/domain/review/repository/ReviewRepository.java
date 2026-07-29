package com.apppang.apppang2.domain.review.repository;

import com.apppang.apppang2.domain.review.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ReviewRepository extends JpaRepository<Review, Long> {

    //orderDetailId로 이미 작성된 리뷰가 있는지 확인
    boolean existsByOrderDetailId(Long orderDetailId);

    //상품Id로 리뷰를 조회하며 Pageable 규격에 따라 페이징 및 정렬 처리
    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.productId = :productId")
    Slice<Review> findReviewsWithUserByProductId(@Param("productId") Long productId, Pageable pageable);

    @Query("SELECT r.imageUrl1 FROM Review r WHERE r.imageUrl1 IS NOT NULL")
    List<String> findAllImageUrl1();

    @Query("SELECT r.imageUrl2 FROM Review r WHERE r.imageUrl2 IS NOT NULL")
    List<String> findAllImageUrl2();
}
