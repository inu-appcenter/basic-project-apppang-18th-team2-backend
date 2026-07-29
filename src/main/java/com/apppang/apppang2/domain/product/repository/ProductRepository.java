package com.apppang.apppang2.domain.product.repository;

import com.apppang.apppang2.domain.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    //파라미터가 null이면 해당 조건을 건너뛰는 방식으로 검색/카테고리/할인 필터를 쿼리 하나로 처리
    //정렬(ORDER BY)은 Pageable에 담긴 Sort가 자동으로 붙음
    @Query("""
            SELECT p FROM Product p
            WHERE (:keyword IS NULL OR p.name LIKE CONCAT('%', :keyword, '%'))
              AND (:categoryId IS NULL OR p.categoryId = :categoryId)
              AND (:discountOnly = FALSE OR p.discountRate > 0)
              AND (:event IS NULL OR :event MEMBER OF p.events)
            """)
    Slice<Product> search(@Param("keyword") String keyword,
                          @Param("categoryId") Long categoryId,
                          @Param("discountOnly") boolean discountOnly,
                          @Param("event") String event,
                          Pageable pageable);

    // 결제 재고 차감에 사용할 Lock
    // 안정성을 챙기기 위해 비관적 락으로 동시성 제어
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    //ORDER BY p.id ASC로 항상 아이디순으로 락을 걸게하여 데드락을 방지
    @Query("SELECT p FROM Product p WHERE p.id IN :ids ORDER BY p.id ASC")
    List<Product> findAllByIdWithLock(@Param("ids") List<Long> ids);


}