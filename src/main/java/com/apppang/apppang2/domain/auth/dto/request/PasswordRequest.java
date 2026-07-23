package com.apppang.apppang2.domain.auth.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PasswordRequest {
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    //이메일 입력하고 재설정 링크 요청할 때
    public static class Request{
        private String email;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    //링크 클릭 후 새 비밀번호 입력해서 변경할 때
    public static class Confirm{
        private String token;           //주소창에 있는 토큰 값
        private String newPassword;     //새 비밀번호
    }
}
