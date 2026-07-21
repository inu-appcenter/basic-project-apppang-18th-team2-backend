package com.apppang.apppang2.domain.review.repository;

import com.apppang.apppang2.domain.review.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReviewRepository extends JpaRepository<Review, Long> {

    //orderDetailId로 이미 작성된 리뷰가 있는지 확인
    boolean existsByOrderDetailId(Long orderDetailId);

    //상품Id로 리뷰를 조회하며 Pageable 규격에 따라 페이징 및 정렬 처리
    Slice<Review> findByProductId(Long productId, Pageable pageable);
}
