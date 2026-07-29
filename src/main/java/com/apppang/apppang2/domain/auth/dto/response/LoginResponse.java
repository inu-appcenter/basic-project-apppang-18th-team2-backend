package com.apppang.apppang2.domain.auth.dto.response;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    @JsonIgnore
    private String refreshToken;
    private UserInfo user;      //응답 데이터 구조의 user

    @Getter
    @Builder
    public static class UserInfo{
        private Long userId;
        private String email;
    }
}
