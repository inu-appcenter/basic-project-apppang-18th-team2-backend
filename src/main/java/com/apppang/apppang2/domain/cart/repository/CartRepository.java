package com.apppang.apppang2.domain.cart.repository;

import com.apppang.apppang2.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    //유저의 장바구니 항목과 연결된 상품을 조회, 최근 담은 상품부터 조회하도록 설정
    @Query("SELECT c FROM Cart c JOIN FETCH c.product WHERE c.userId = :userId ORDER BY c.id DESC")
    List<Cart> findAllWithProductByUserId(@Param("userId") Long userId);

    //유저가 이 상품을 이미 담았는지 확인
    Optional<Cart> findByUserIdAndProductId(Long userId, Long productId);

    //내 장바구니 조회, 수량조절,삭제용
    Optional<Cart> findByIdAndUserId(Long id, Long userId);

    //유저ID로 해당 유저의 장바구니 상품을 모두 삭제
    void deleteByUserId(Long userId);

    //특정 유저의 장바구니에서 특정 상품 ID 리스트에 해당하는 항목들을 일괄 삭제
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.userId = :userId AND c.product.id IN :productIds")
    void deleteByUserIdAndProductIdIn(@Param("userId") Long userId, @Param("productIds") List<Long> productIds);
}