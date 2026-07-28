package com.apppang.apppang2.domain.user.repository;

import com.apppang.apppang2.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //이메일로 가입 여부 검사
    Boolean existsByEmailAndDeletedFalse(String email);

    //가입된 유저인지 확인
    Optional<User> findByEmail(String email);

    //AddressService에서 주소 추가를 위해 Id로 가입된 유저인지 확인
    Optional<User> findById(Long userId);

    //이메일로 탈퇴하지 않은 유저 찾기
    Optional<User> findByEmailAndDeletedFalse(String email);

    //이름과 전화번호가 일치하는 탈퇴하지 않은 유저 찾기
    Optional<User> findByNameAndPhoneAndDeletedFalse(String name, String phone);

    // 내 정보 조회, 수정, 탈퇴용
    Optional<User> findByIdAndDeletedFalse(Long id);
}
