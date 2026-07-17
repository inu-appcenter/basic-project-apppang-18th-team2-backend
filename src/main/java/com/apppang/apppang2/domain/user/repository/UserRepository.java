package com.apppang.apppang2.domain.user.repository;

import com.apppang.apppang2.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //이메일로 가입 여부 검사
    Boolean existsByEmail(String email);

    //가입된 유저인지 확인
    Optional<User> findByEmail(String email);

    //AddressService에서 주소 추가를 위해 Id로 가입된 유저인지 확인
    Optional<User> findById(Long userId);

    //이름과 전화번호가 일치하는 유저 찾기
    Optional<User> findByNameAndPhone(String name, String phone);
}
