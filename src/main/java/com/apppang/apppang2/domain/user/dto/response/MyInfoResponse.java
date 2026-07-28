package com.apppang.apppang2.domain.user.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyInfoResponse {
    private Long userId;
    private String email;
    private String name;
    private String phone;
    private String profileImage;

    @Builder
    public MyInfoResponse(Long userId, String email, String name, String phone, String profileImage){
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.profileImage = profileImage;
    }
}
