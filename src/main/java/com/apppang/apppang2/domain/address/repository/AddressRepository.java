package com.apppang.apppang2.domain.address.repository;

import com.apppang.apppang2.domain.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface AddressRepository extends JpaRepository<Address, Long> {

    //유저ID로 등록된 모든 배송지 조회
    List<Address> findByUserId(Long userId); //조회결과 여러개일때 List 사용

    //유저ID로 유저의 기존 기본 배송지를 찾아오는 메서드
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);    //있을수도 없을수도 있어 Optional 사용

    //배송지ID로 배송지 조회
    Optional<Address> findById(Long id);

    //내 배송지만 조회 (남의 배송지로 주문 방지)
    Optional<Address> findByIdAndUserId(Long id, Long userId);

}
