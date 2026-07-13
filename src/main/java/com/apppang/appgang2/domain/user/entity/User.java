package com.apppang.appgang2.domain.user.entity;

import com.apppang.appgang2.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, name = "user_name")
    private String userName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name="profile_image")
    private String profileImage;   //이미지 주소 URL 저장

    //가입일과 수정일은 BaseTimeEntity에서 상속받아 자동 관리

    @Column(nullable = false, name = "agree_required_terms")
    private Boolean agreeRequiredTerms;

    //선택이더라도 동의 또는 비동의 두 가지 상태만 존재해야하므로 Not null
    @Column(nullable = false, name = "agree_marketing")
    private Boolean agreeMarketing;
}
