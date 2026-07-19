package com.apppang.apppang2.domain.user.entity;

import com.apppang.apppang2.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, name = "user_name")
    private String name;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name="profile_image")
    private String profileImage;   //이미지 주소 URL 저장

    //가입일과 수정일은 BaseTimeEntity에서 상속받아 자동 관리

    @Column(nullable = false, name = "agree_required_terms")
    private boolean agreeRequiredTerms;

    @Column(nullable = false, name = "agree_marketing")
    private boolean agreeMarketing;

    @Column(nullable = false)
    private boolean deleted;

    @Builder
    public User(String email, String password, String name, String phone, Role role, boolean agreeRequiredTerms, boolean agreeMarketing, boolean deleted){
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.agreeRequiredTerms = agreeRequiredTerms;
        this.agreeMarketing = agreeMarketing;
        this.deleted = deleted;
    }

    public void updateMyInfo(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public void delete() {
        this.deleted = true;
        this.email = "deleted_" + this.id + "_" + this.email;
    }

    //비밀번호 재설정
    public void updatePassword(String newPassword){
        this.password = newPassword;
    }

}
