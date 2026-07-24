package com.apppang.apppang2.domain.wishlist.repository;

import com.apppang.apppang2.domain.wishlist.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    //찜 목록 조회(N+1 문제 방지를 위한 Fetch Join)
    @Query("SELECT w FROM Wishlist w JOIN FETCH w.product WHERE w.user.id = :userId")
    List<Wishlist> findAllByUserIdWithProduct(@Param("userId") Long userId);    //쿼리의 userId와 매핑하겠다고 선언

    //특정 사용자가 해당 상품을 이미 찜했는지 확인
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    //유저ID와 상품ID로 찜 엔티티 찾기
    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);

}
